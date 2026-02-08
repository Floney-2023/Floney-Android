package com.aos.floney.view.book.setting.budget

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.aos.data.util.CurrencyUtil
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.formatNumber
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.book.BudgetItem
import com.aos.usecase.booksetting.BooksInfoBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookSettingBudgetBottomSheetViewModel @Inject constructor(
    private val app: Application,
    private val prefs: SharedPreferenceUtil,
    private val booksInfoBudgetUseCase: BooksInfoBudgetUseCase
): BaseViewModel() {


    var title = MutableLiveData<String>("")

    private var _monthBudget = MutableLiveData<String>("")
    val monthBudget: LiveData<String> get() = _monthBudget

    // 월 예산 설정
    private var _budgetSheet = MutableEventFlow<String>()
    val budgetSheet: EventFlow<String> get() = _budgetSheet

    // 금액
    var cost = MutableLiveData<String>("")

    // 요청 날짜
    var date = MutableLiveData<String>("")


    fun settingUi(item: BudgetItem, dateString: String){

        date.postValue(dateString)
        // Use formatted string resource for title
        title.postValue(app.getString(R.string.month_budget_format, item.month))

        if (!item.money.equals("0${CurrencyUtil.currency}"))
            cost.postValue(item.money)
        else
            cost.postValue("")
    }

    // 예산 저장
    fun onClickSaveButton(){
        viewModelScope.launch {

            baseEvent(Event.ShowLoading)
            booksInfoBudgetUseCase(
                prefs.getString("bookKey",""),
                settingCost(),
                date.value!! ).onSuccess {

                baseEvent(Event.HideLoading)

                if (cost.value!! == "")
                    _budgetSheet.emit("0${CurrencyUtil.currency}")
                else
                    _budgetSheet.emit(cost.value!!)

            }.onFailure {
                baseEvent(Event.HideLoading)
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@BookSettingBudgetBottomSheetViewModel)))
            }
        }
    }
    fun settingCost(): Long {
        if (cost.value=="")
            return 0
        else if (cost.value!!.length<=4)
            return cost.value!!.substring(0, cost.value!!.length-1).toLong()
        else if (cost.value!="")
            return cost.value!!.replace(",", "").replace(CurrencyUtil.currency,"").toLong()
        else
            return 0
    }
    // bottomsheet 내리기
    fun onClickedInitBtn(){
        cost.postValue("")
    }

    // 숫자 변화 감지
    fun onCostTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (count == 0) {
            cost.postValue("${s.toString().formatNumber()}")
        } else {
            cost.postValue("${s.toString().formatNumber()}${CurrencyUtil.currency}")
        }
    }
}