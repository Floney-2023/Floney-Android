package com.aos.floney.view.book.setting.excel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.book.UiBookCategory
import com.aos.usecase.booksetting.BooksExcelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class BookSettingExcelViewModel @Inject constructor(
    private val application: Application,
    private val prefs: SharedPreferenceUtil,
    private val booksExcelUseCase: BooksExcelUseCase
) : BaseViewModel() {

    // 이전 페이지
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back

    // 엑셀 선택
    val _excelItem = MutableLiveData<List<UiBookCategory>>()
    val excelItem: LiveData<List<UiBookCategory>> get() = _excelItem

    private var _excelClickItem = MutableLiveData<UiBookCategory?>()
    val excelClickItem: LiveData<UiBookCategory?> get() = _excelClickItem



    // 내보내기 엑셀
    private var _completePage = MutableEventFlow<ResponseBody>()
    val completePage: EventFlow<ResponseBody> get() = _completePage

    // 이번달, 저번달, 올해, 작년, 전체
    var flow = MutableLiveData<String>()


    init {
        val thisMonth = application.getString(R.string.excel_export_this_month)
        flow.value = thisMonth

        val array = arrayListOf<UiBookCategory>(
            UiBookCategory(0, true, thisMonth, null, false),
            UiBookCategory(1, false, application.getString(R.string.excel_export_last_month), null, false),
            UiBookCategory(2, false, application.getString(R.string.excel_export_this_year), null, false),
            UiBookCategory(3, false, application.getString(R.string.excel_export_last_year), null, false),
            UiBookCategory(4, false, application.getString(R.string.excel_export_all), null, false),
        )
        _excelClickItem.value = array[0]
        _excelItem.postValue(array)
    }

    fun onClickedExit() {
        viewModelScope.launch {
            _back.emit(true)
        }
    }

    // 엑셀 내보내기
    fun onClickAddComplete() {
        if (_excelClickItem.value!!.name.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                booksExcelUseCase(
                    bookKey = prefs.getString("bookKey", ""),
                    getExcelDuration(_excelClickItem.value!!.name),
                    getCurrentDate(_excelClickItem.value!!.name)
                ).onSuccess {
                    _completePage.emit(it)
                }.onFailure {
                    baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@BookSettingExcelViewModel)))
                }
            }
        } else{
            baseEvent(Event.ShowToast("항목 이름을 입력해주세요."))
        }

    }


    fun onClickRepeatItem(_item: UiBookCategory) {
        val item = _excelItem.value?.map {
            UiBookCategory(
                it.idx, false, it.name, it.categoryKey, it.default
            )
        } ?: listOf()
        item[_item.idx].checked = !item[_item.idx].checked
        _excelItem.postValue(item)
        _excelClickItem.value = _item
    }

    // 이번달, 저번달, 올해, 작년, 전체 클릭
    fun onClickFlow(type: String) {
        flow.postValue(type)
    }
    private fun getCurrentDate(category: String): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val thisMonth = application.getString(R.string.excel_export_this_month)
        val lastMonth = application.getString(R.string.excel_export_last_month)
        val thisYear = application.getString(R.string.excel_export_this_year)
        val lastYear = application.getString(R.string.excel_export_last_year)
        val all = application.getString(R.string.excel_export_all)

        return when (category) {
            thisMonth -> {
                currentDate.withDayOfMonth(1).format(formatter) // 이번 달 1일
            }
            lastMonth -> {
                currentDate.minusMonths(1).withDayOfMonth(1).format(formatter) // 저번 달 1일
            }
            lastYear -> {
                currentDate.minusYears(1).withDayOfYear(1).format(formatter) // 작년 1월 1일
            }
            thisYear -> {
                currentDate.withDayOfYear(1).format(formatter) // 올해 1월 1일
            }
            all -> {
                currentDate.withDayOfMonth(1).format(formatter) // 이번 달 1일
            }
            else -> ""
        }
    }
    private fun getExcelDuration(category: String): String {
        val thisMonth = application.getString(R.string.excel_export_this_month)
        val lastMonth = application.getString(R.string.excel_export_last_month)
        val thisYear = application.getString(R.string.excel_export_this_year)
        val lastYear = application.getString(R.string.excel_export_last_year)
        val all = application.getString(R.string.excel_export_all)

        return when (category) {
            thisMonth -> {
                "THIS_MONTH"
            }

            lastMonth -> {
                "LAST_MONTH"
            }

            lastYear -> {
                "LAST_YEAR"
            }

            thisYear -> {
                "ONE_YEAR"
            }

            all -> {
                "ALL"
            }

            else -> ""
        }
    }
}