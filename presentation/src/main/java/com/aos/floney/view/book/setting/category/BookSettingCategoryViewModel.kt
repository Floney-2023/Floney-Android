package com.aos.floney.view.book.setting.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.formatNumber
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.book.UiBookCategory
import com.aos.model.home.DayMoneyModifyItem
import com.aos.usecase.booksetting.BooksCategoryDeleteUseCase
import com.aos.usecase.history.GetBookCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookSettingCategoryViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val getBookCategoryUseCase: GetBookCategoryUseCase,
    private val booksCategoryDeleteUseCase: BooksCategoryDeleteUseCase
) : BaseViewModel() {

    // 닉네임
    private val _nickname = MutableLiveData<String>()
    val nickname: LiveData<String> get() = _nickname


    // 이전 페이지
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back

    // 추가 페이지
    private var _addPage = MutableEventFlow<Boolean>()
    val addPage: EventFlow<Boolean> get() = _addPage

    // 분류 항목 관리 모드 편집 / 완료
    var edit = MutableLiveData<Boolean>(false)

    // 자산, 지출, 수입, 이체
    var flow = MutableLiveData<String>("자산")

    // 내용
    var _categoryList = MutableLiveData<List<UiBookCategory>>()
    val categoryList: LiveData<List<UiBookCategory>> get() = _categoryList

    init {
        getBookCategory()
    }

    fun onClickPreviousPage() {
        viewModelScope.launch {
            if (edit.value!! && categoryList.value!!.isNotEmpty())
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
            getBookCategoryUseCase(prefs.getString("bookKey", ""), flow.value!!).onSuccess { it ->
                val item = it.map {
                    UiBookCategory(
                        it.idx, edit.value!!, it.name, it.default
                    )
                }
                _categoryList.postValue(item)

            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@BookSettingCategoryViewModel)))
            }
        }
    }

    // 저장버튼 클릭
    fun onClickSaveBtn() {
        viewModelScope.launch {
            _addPage.emit(true)
        }
    }
    // 편집버튼 클릭
    fun onClickEdit(){
        edit.value = !edit.value!!
        getBookCategory()
    }

    // 내역 삭제
    fun deleteCategory(item : UiBookCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            booksCategoryDeleteUseCase(
                bookKey = prefs.getString("bookKey", ""),
                flow.value!!,
                item.name
            ).onSuccess {
                val updatedList = _categoryList.value!!.filter { it.name != item.name }
                _categoryList.postValue(updatedList)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@BookSettingCategoryViewModel)))
            }
        }
    }

    // 자산, 지출, 수입, 이체 클릭
    fun onClickFlow(type: String) {
        flow.value = type
        getBookCategory()
    }
}