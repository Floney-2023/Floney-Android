package com.aos.floney.view.book.setting.favorite

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.data.util.SubscriptionDataStoreUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.ext.toCategoryCode
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.floney.util.getAdvertiseTenMinutesCheck
import com.aos.model.book.UiBookFavoriteModel
import com.aos.usecase.booksetting.BooksFavoriteDeleteUseCase
import com.aos.usecase.history.GetBookFavoriteUseCase
import com.aos.usecase.subscribe.SubscribeBenefitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BookSettingFavoriteViewModel @Inject constructor(
    private val application: Application,
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

    // 추가 버튼 클릭
    fun onClickAddBtn() {
        viewModelScope.launch(Dispatchers.IO) {
            // 구독자면 바로 패스
            if (subscriptionDataStoreUtil.getBookSubscribe().first()) {
                _addPage.emit(true)
                return@launch
            }

            try {
                val bookKey = prefs.getString("bookKey", "")

                val categories = listOf("지출", "수입", "이체")
                var totalCount = 0

                for (category in categories) {
                    val result = getBookFavoriteUseCase(
                        bookKey,
                        category.toCategoryCode()
                    )

                    if (result.isFailure) {
                        baseEvent(Event.ShowToast(result.exceptionOrNull()?.message.parseErrorMsg()))
                        return@launch
                    }

                    val count = result.getOrNull()?.size ?: 0
                    totalCount += count

                    // 누적합 15 초과 시 즉시 종료
                    if (totalCount >= 15) {
                        _subscribePrompt.emit(true)
                        return@launch
                    }
                }

                // 여기까지 왔으면 15 미만 → 추가 페이지 이동
                _addPage.emit(true)

            } catch (e: Exception) {
                baseEvent(Event.ShowToast(e.message.parseErrorMsg()))
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
                baseEvent(Event.ShowSuccessToast(application.getString(R.string.toast_history_deleted)))
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