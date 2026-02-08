package com.aos.floney.view.settleup

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.settlement.BookUsers
import com.aos.model.settlement.UiMemberSelectModel
import com.aos.usecase.settlement.BooksOutComesUseCase
import com.aos.usecase.settlement.BooksUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettleUpPeriodSelectViewModel @Inject constructor(
    private val app: Application,
    stateHandle: SavedStateHandle,
    private val prefs: SharedPreferenceUtil,
    private val booksOutComesUseCase : BooksOutComesUseCase
): BaseViewModel() {


    var memberArray: LiveData<Array<String>> = stateHandle.getLiveData("member")

    // 선택 날짜 String format
    private var _selectDay = MutableLiveData<String>("")
    val selectDay: LiveData<String> get() = _selectDay

    // 시작 날짜
    private var _startDay = MutableLiveData<String>("")
    val startDay: LiveData<String> get() = _startDay

    // 끝 날짜
    private var _endDay = MutableLiveData<String>("")
    val endDay: LiveData<String> get() = _endDay

    // 다음 정산 페이지
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back

    // 다음 정산 페이지
    private var _nextPage = MutableEventFlow<Boolean>()
    val nextPage: EventFlow<Boolean> get() = _nextPage

    // 처음 정산하기 페이지
    private var _settlementPage = MutableEventFlow<Boolean>()
    val settlementPage: EventFlow<Boolean> get() = _settlementPage

    // bottomSheet 불러오기
    // 처음 정산하기 페이지
    private var _periodBottomSheetPage = MutableEventFlow<Boolean>()
    val periodBottomSheetPage: EventFlow<Boolean> get() = _periodBottomSheetPage

    // bottomSheet 불러오기
    fun onClickedPeriodSelect(){
        viewModelScope.launch {
            _periodBottomSheetPage.emit(true)
        }
    }
    // 정산 내역 체크하러 가기
    fun onClickedNextPage() {
        if (startDay.value!="" && endDay.value!="")
        {
            val userEmails = memberArray.value!!.map { it }
            viewModelScope.launch(Dispatchers.IO) {
                baseEvent(Event.ShowLoading)
                booksOutComesUseCase(userEmails, startDay.value!!, endDay.value!!, prefs.getString("bookKey","")).onSuccess {
                    if (it!!.outcomes.isEmpty())
                    {
                        baseEvent(Event.HideLoading)
                        baseEvent(Event.ShowToastRes(R.string.settle_up_period_select_error_messsage))
                    }
                    else {
                        // 내역 존재 시 이동
                        _nextPage.emit(true)
                        baseEvent(Event.HideLoading)
                    }
                }.onFailure {
                    baseEvent(Event.HideLoading)
                    baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@SettleUpPeriodSelectViewModel)))
                }
            }
        }
        else {
            baseEvent(Event.ShowToast(app.getString(R.string.toast_set_period)))
        }
    }
    // 이전 페이지로
    fun onClickedBackPage(){
        viewModelScope.launch {
            _back.emit(true)
        }
    }
    // exit 버튼 클릭 -> 처음 정산하기 페이지
    fun onClickedExit() {
        viewModelScope.launch {
            _settlementPage.emit(true)
        }
    }

    // 날짜 선택 범위 text 세팅
    fun settingSelectDay(settingFormat: String, startSelectDay: String, endSelectDay: String){
        _startDay.value = startSelectDay
        _endDay.value = endSelectDay

        viewModelScope.launch(Dispatchers.IO) {
            _selectDay.postValue(settingFormat)
        }
    }
}