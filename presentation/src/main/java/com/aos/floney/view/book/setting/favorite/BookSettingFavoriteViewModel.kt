package com.aos.floney.view.book.setting.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.data.util.SubscriptionDataStoreUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.ext.toCategoryCode
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.book.UiBookFavoriteModel
import com.aos.usecase.booksetting.BooksFavoriteDeleteUseCase
import com.aos.usecase.history.GetBookFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookSettingFavoriteViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val getBookFavoriteUseCase: GetBookFavoriteUseCase,
    private val booksFavoriteDeleteUseCase: BooksFavoriteDeleteUseCase,
    private val subscriptionDataStoreUtil: SubscriptionDataStoreUtil
) : BaseViewModel() {

    // 구독 유도 팝업
    private var _subscribePrompt = MutableEventFlow<Boolean>()
    val subscribePrompt: EventFlow<Boolean> get() = _subscribePrompt

    // 이전 페이지
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back

    // 추가 페이지
    private var _addPage = MutableEventFlow<Boolean>()
    val addPage: EventFlow<Boolean> get() = _addPage

    // 분류 항목 관리 모드 편집 / 완료
    var edit = MutableLiveData<Boolean>(false)

    // 자산, 지출, 수입, 이체
    var flow = MutableLiveData<String>("지출")

    // 내용
    var _favoriteList = MutableLiveData<List<UiBookFavoriteModel>>()
    val favoriteList: LiveData<List<UiBookFavoriteModel>> get() = _favoriteList

    init {
        getBookCategory()
    }

    fun onClickPreviousPage() {
        viewModelScope.launch {
            if (edit.value!!&& favoriteList.value!!.isNotEmpty())
            {
                _back.emit(false)
            }
            else{
                _back.emit(true)
            }
        }
    }
    // 자산/분류 카테고리 항목 가져오기
    fun getBookCategory() {
        viewModelScope.launch(Dispatchers.IO) {
            getBookFavoriteUseCase(prefs.getString("bookKey", ""), flow.value!!.toCategoryCode()).onSuccess { it ->
                val item = it.map {
                    UiBookFavoriteModel(
                        it.idx, edit.value!!, it.description,it.lineCategoryName,it.lineSubcategoryName,it.assetSubcategoryName, it.money, it.exceptStatus
                    )
                }
                _favoriteList.postValue(item)

            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@BookSettingFavoriteViewModel)))
            }
        }
    }

    // 추가하기 버튼 클릭
    fun onClickAddBtn() {
        viewModelScope.launch {
            // 구독자면 그냥 패스
            if (subscriptionDataStoreUtil.getBookSubscribe().first()) {
                _addPage.emit(true)
                return@launch
            }

            baseEvent(Event.ShowLoading)

            val categories = listOf("수입", "이체", "지출")
            val overLimit = categories.any { category ->
                val result = getBookFavoriteUseCase(prefs.getString("bookKey", ""), category.toCategoryCode())
                val count = result.getOrNull()?.size ?: 0

                if (result.isFailure) {
                    baseEvent(Event.HideLoading)
                    baseEvent(Event.ShowToast(result.exceptionOrNull()?.message.parseErrorMsg(this@BookSettingFavoriteViewModel)))
                    return@any false // 중단
                }

                count >= 5 // 하나라도 5개 이상이면 true
            }

            baseEvent(Event.HideLoading)
            if (overLimit) {
                _subscribePrompt.emit(true)
            } else {
                _addPage.emit(true)
            }
        }
    }


    // 편집버튼 클릭
    fun onClickEdit(){
        edit.value = !edit.value!!
        getBookCategory()
    }

    // 내역 삭제
    fun deleteFavorite(item : UiBookFavoriteModel) {
        viewModelScope.launch(Dispatchers.IO) {
            baseEvent(Event.ShowLoading)
            booksFavoriteDeleteUseCase(
                bookKey = prefs.getString("bookKey", ""),
                item.idx
            ).onSuccess {
                val updatedList = _favoriteList.value!!.filter { it.idx != item.idx }
                _favoriteList.postValue(updatedList)

                baseEvent(Event.HideLoading)
                baseEvent(Event.ShowSuccessToast("삭제가 완료되었습니다."))
            }.onFailure {
                baseEvent(Event.ShowLoading)
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@BookSettingFavoriteViewModel)))
            }
        }
    }

    // 자산, 지출, 수입, 이체 클릭
    fun onClickFlow(type: String) {
        flow.value = type
        getBookCategory()
    }
}