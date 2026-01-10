package com.aos.floney.view.settleup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.user.MyBooks
import com.aos.usecase.settlement.SettlementLastUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettleUpStartViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val settlementLastUseCase : SettlementLastUseCase
): BaseViewModel() {


    private var _lastDay = MutableLiveData<Int>(0)
    val lastDay: LiveData<Int> get() = _lastDay


    // 정산하기 페이지
    private var _settleUpStartPage = MutableEventFlow<Boolean>()
    val settleUpStartPage: EventFlow<Boolean> get() = _settleUpStartPage

    // 정산하기 페이지
    private var _settleUpSeePage = MutableEventFlow<Boolean>()
    val settleUpSeePage: EventFlow<Boolean> get() = _settleUpSeePage


    init {
        getSettleUpInform()
    }

    // 정산 내역 날짜 불러오기
    fun getSettleUpInform(){
        viewModelScope.launch(Dispatchers.IO) {
            settlementLastUseCase(prefs.getString("bookKey","")).onSuccess {
                // 전송 성공
                _lastDay.postValue(it.passedDays.toInt())
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
            }
        }
    }
    // 정산하기 시작
    fun onClickedSettleUpStart() {
        viewModelScope.launch {
            _settleUpStartPage.emit(true)
        }
    }

    // 정산 내역 보기 클릭
    fun onClickedSettleUpSee() {
        viewModelScope.launch {
            _settleUpSeePage.emit(true)
        }
    }

}