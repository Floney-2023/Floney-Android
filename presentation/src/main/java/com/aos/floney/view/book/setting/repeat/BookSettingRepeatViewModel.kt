package com.aos.floney.view.book.setting.repeat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.formatNumber
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.ext.toCategoryCode
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.book.UiBookCategory
import com.aos.model.book.UiBookRepeatModel
import com.aos.model.home.DayMoneyModifyItem
import com.aos.usecase.booksetting.BooksCategoryDeleteUseCase
import com.aos.usecase.booksetting.BooksRepeatDeleteUseCase
import com.aos.usecase.booksetting.BooksRepeatGetUseCase
import com.aos.usecase.history.GetBookCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookSettingRepeatViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val booksRepeatGetUseCase: BooksRepeatGetUseCase,
    private val booksRepeatDeleteUseCase: BooksRepeatDeleteUseCase
) : BaseViewModel() {

    // 내역 삭제
    private var _deleteBooksLines = MutableEventFlow<Boolean>()
    val deleteBooksLines: EventFlow<Boolean> get() = _deleteBooksLines

    // 내역 수정 결과
    private var _postModifyBooksLines = MutableEventFlow<Boolean>()
    val postModifyBooksLines: EventFlow<Boolean> get() = _postModifyBooksLines

    // 날짜 클릭 여부
    private var _showCalendar = MutableEventFlow<Boolean>()
    val showCalendar: EventFlow<Boolean> get() = _showCalendar

    // 닫기 클릭
    private var _onClickCloseBtn = MutableEventFlow<Boolean>()
    val onClickCloseBtn: EventFlow<Boolean> get() = _onClickCloseBtn

    // 카테고리 클릭
    private var _onClickCategory = MutableEventFlow<Boolean>()
    val onClickCategory: EventFlow<Boolean> get() = _onClickCategory

    // 날짜
    private var tempDate = ""
    var date = MutableLiveData<String>("날짜를 선택하세요")

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

    // 지출, 수입, 이체
    var flow = MutableLiveData<String>("지출")

    // 반복 내역 내용
    var _repeatList = MutableLiveData<List<UiBookRepeatModel>>()
    val repeatList: LiveData<List<UiBookRepeatModel>> get() = _repeatList

    init {
        getBookCategory()
    }

    fun onClickPreviousPage() {
        viewModelScope.launch {
            if (edit.value!! && repeatList.value!!.isNotEmpty())
            {
                _back.emit(false)
            }
            else{
                _back.emit(true)
            }
        }
    }
    // 자산/분류 카테고리 항목 가져오기
    private fun getBookCategory() {
        viewModelScope.launch(Dispatchers.IO) {
            booksRepeatGetUseCase(prefs.getString("bookKey", ""), flow.value!!.toCategoryCode()).onSuccess { it ->

                _repeatList.postValue(it)

            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@BookSettingRepeatViewModel)))
            }
        }
    }
    // 편집버튼 클릭
    fun onClickEdit(){
        edit.value = !edit.value!!

        val item = _repeatList.value?.map {
            UiBookRepeatModel(
                it.id, it.description, it.repeatDuration, it.lineSubCategory, it.assetSubCategory, it.money, edit.value!!)
        } ?: listOf()
        _repeatList.postValue(item)
    }

    // 반복 내역 삭제
    fun deleteCategory(item : UiBookRepeatModel) {
        viewModelScope.launch(Dispatchers.IO) {
            baseEvent(Event.ShowLoading)
            booksRepeatDeleteUseCase(
                item.id
            ).onSuccess {
                val updatedList = _repeatList.value!!.filter { it.id != item.id }
                _repeatList.postValue(updatedList)

                baseEvent(Event.ShowSuccessToast("변경이 완료되었습니다."))
                baseEvent(Event.HideLoading)
            }.onFailure {
                baseEvent(Event.HideLoading)
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@BookSettingRepeatViewModel)))
            }
        }
    }

    // 자산, 지출, 수입, 이체 클릭
    fun onClickFlow(type: String) {
        flow.value = type
        getBookCategory()
    }
}