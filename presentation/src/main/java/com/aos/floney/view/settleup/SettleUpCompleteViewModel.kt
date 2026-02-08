package com.aos.floney.view.settleup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.BuildConfig
import com.aos.floney.BuildConfig.appsflyer_settlement_url
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.bookCodeToSettlementUrl
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.floney.util.getCurrentDateTimeString
import com.aos.model.book.UiBookSettingModel
import com.aos.model.settlement.UiSettlementAddModel
import com.aos.model.settlement.settleOutcomes
import com.aos.usecase.booksetting.BooksCodeCheckUseCase
import com.aos.usecase.booksetting.BooksSettingGetUseCase
import com.aos.usecase.mypage.AlarmSaveGetUseCase
import com.aos.usecase.settlement.SettlementAddUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettleUpCompleteViewModel @Inject constructor(
    stateHandle: SavedStateHandle,
    private val prefs: SharedPreferenceUtil,
    private val settlementAddUseCase : SettlementAddUseCase,
    private val booksSettingGetUseCase : BooksSettingGetUseCase,
    private val alarmSaveGetUseCase : AlarmSaveGetUseCase,
    private val booksCodeCheckUseCase: BooksCodeCheckUseCase
): BaseViewModel() {


    var memberArray: LiveData<Array<String>> = stateHandle.getLiveData("member")
    var startDate: LiveData<String> = stateHandle.getLiveData("startDay")
    var endDate: LiveData<String> = stateHandle.getLiveData("endDay")
    var outcome: LiveData<LongArray> = stateHandle.getLiveData("outcome")
    var userEmail: LiveData<Array<String>> = stateHandle.getLiveData("userEmail")

    private var _settlementModel = MutableLiveData<UiSettlementAddModel>()
    val settlementModel: LiveData<UiSettlementAddModel> get() = _settlementModel

    // 이전 페이지
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back

    // 다음 정산 페이지
    private var _nextPage = MutableEventFlow< Array<settleOutcomes>>()
    val nextPage: EventFlow< Array<settleOutcomes>> get() = _nextPage

    // 처음 정산하기 페이지
    private var _settlementPage = MutableEventFlow<Boolean>()
    val settlementPage: EventFlow<Boolean> get() = _settlementPage

    // 가계부 정보
    private var _bookSettingInfo = MutableLiveData<UiBookSettingModel>()
    val bookSettingInfo: LiveData<UiBookSettingModel> get() = _bookSettingInfo

    // 정보 load 완료
    private var _getInform = MutableEventFlow<Boolean>()
    val getInform: EventFlow<Boolean> get() = _getInform


    // 처음 정산하기 페이지
    private var _settlementSharePage = MutableEventFlow<String>()
    val settlementSharePage: EventFlow<String> get() = _settlementSharePage

    init {
        getOutcomesItems()
    }

    fun getOutcomesItems(){
        val userEmails = memberArray.value!!.map { it }
        val outcomes = outcome.value!!.zip(userEmail.value!!) { outcome, email ->
            settleOutcomes(outcome, email)
        }
        Timber.e("yeah : ${outcomes}")
        viewModelScope.launch(Dispatchers.IO) {
            settlementAddUseCase(prefs.getString("bookKey",""), startDate.value!!, endDate.value!!,userEmails, outcomes).onSuccess {
                // 불러오기 성공
                _settlementModel.postValue(it)
                searchBookSettingItems()
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@SettleUpCompleteViewModel)))
            }
        }
    }

    fun searchBookSettingItems()
    {
        viewModelScope.launch(Dispatchers.IO) {
            booksSettingGetUseCase(prefs.getString("bookKey","")).onSuccess {
                // me가 true인 항목이 맨 앞에 오도록 정렬
                val sortedList = it.ourBookUsers.sortedByDescending { it.me }
                _bookSettingInfo.postValue(it.copy(ourBookUsers = sortedList))
                _getInform.emit(true)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
            }
        }
    }
    fun saveInviteAlarm(){
        viewModelScope.launch {
            if(prefs.getString("bookKey","").isNotEmpty()) {
                baseEvent(Event.ShowLoading)
                memberArray.value?.map {
                    alarmSaveGetUseCase(
                        prefs.getString("bookKey",""),
                        "플로니",
                        "${_bookSettingInfo.value!!.bookName} 가계부를 정산해보세요.",
                        "icon_noti_settlement",
                        it,
                        getCurrentDateTimeString()
                    ).onSuccess {
                        baseEvent(Event.HideLoading)
                    }.onFailure {
                        baseEvent(Event.HideLoading)
                        baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
                    }
                }

            }
        }
    }
    // 공유하기
    fun onClickedSharePage(){

        viewModelScope.launch(Dispatchers.IO) {
            baseEvent(Event.ShowLoading)
            booksCodeCheckUseCase(
                prefs.getString("bookKey","")).onSuccess {
                baseEvent(Event.HideLoading)
                _settlementSharePage.emit(it.code.bookCodeToSettlementUrl(settlementModel.value!!.id))
            }.onFailure {
                baseEvent(Event.HideLoading)
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@SettleUpCompleteViewModel)))
            }
        }
    }
    // exit 버튼 클릭 -> 처음 정산하기 페이지
    fun onClickedExit() {
        viewModelScope.launch {
            _settlementPage.emit(true)
        }
    }
}