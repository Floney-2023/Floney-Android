package com.aos.floney.view.book.setting.budget

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.CurrencyUtil
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.book.BudgetItem
import com.aos.model.book.UiBookBudgetModel
import com.aos.usecase.booksetting.BooksBudgetCheckUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BookSettingBudgetViewModel @Inject constructor(
    private val application: Application,
    private val prefs: SharedPreferenceUtil,
    private val booksBudgetCheckUseCase : BooksBudgetCheckUseCase
): BaseViewModel() {

    // 예산 리스트
    private var _budgetList = MutableLiveData<UiBookBudgetModel>()
    val budgetList: LiveData<UiBookBudgetModel> get() = _budgetList


    // 년도 선택
    private var _yearSetting = MutableEventFlow<Boolean>()
    val yearSetting: EventFlow<Boolean> get() = _yearSetting

    // 이전 페이지
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back

    // 처음 정산하기 페이지
    private var _budgetSettingPage = MutableEventFlow<BudgetItem>()
    val budgetSettingPage: EventFlow<BudgetItem> get() = _budgetSettingPage

    private var _year = MutableLiveData<String>()
    val year: LiveData<String> get() = _year

    init {
        // 현재 년도 설정
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        val yearStringFormat = String.format("%s-01-01", currentYear)

        // 예산 내역 불러오기
        getBudgetInform(currentYear, yearStringFormat)
    }

    fun getBudgetInform(currentYear: String, yearStringFormat: String) {
        viewModelScope.launch(Dispatchers.IO) {
            booksBudgetCheckUseCase(prefs.getString("bookKey",""),yearStringFormat).onSuccess {
                // 불러오기 성공
                _budgetList.postValue(it)
                _year.postValue(currentYear)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@BookSettingBudgetViewModel)))
            }
        }
    }
    // 날짜 선택
    fun onClickedYear()
    {
        viewModelScope.launch {
            _yearSetting.emit(true)
        }
    }
    // 이전 페이지
    fun onClickedExit() {
        viewModelScope.launch {
            _back.emit(true)
        }
    }

    // 정산 내역 클릭 시, 정산 내역 detail 하게 보여주기
    fun settingBudget(budgetitem: BudgetItem) {
        viewModelScope.launch {
            _budgetSettingPage.emit(budgetitem)
        }
    }

    fun convertMonthStringToDateString(monthName: String): String {
        val locales = listOf(Locale.KOREAN, Locale.ENGLISH)

        for (locale in locales) {
            try {
                val sdf = SimpleDateFormat("MMMM", locale)
                sdf.isLenient = false

                val date = sdf.parse(monthName) ?: continue
                val cal = Calendar.getInstance().apply { time = date }
                val monthNumber = cal.get(Calendar.MONTH) + 1

                return "%04d-%02d-01".format(year, monthNumber)
            } catch (_: Exception) { }
        }

        return "2026-01-01"
    }

    fun updateBudget(budgetItem: BudgetItem, budgetMoney: String){
        viewModelScope.launch {
            val budgetInfo = budgetList.value ?: return@launch // budgetList가 null이면 함수 종료

            val updatedList = budgetInfo.budgetList.map { item ->
                if (item.month == budgetItem.month) {
                    item.copy(money = budgetMoney, isExist = !budgetMoney.equals("0${CurrencyUtil.currency}"))
                } else {
                    item // 변경 없음
                }
            }
            // 변경 완료 토스트 메세지
            baseEvent(Event.ShowSuccessToast(application.getString(R.string.toast_change_successed)))
            _budgetList.postValue(UiBookBudgetModel(updatedList))
        }
    }
}