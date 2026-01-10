package com.aos.floney.view.settleup

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.data.util.SubscriptionDataStoreUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.floney.util.getAdvertiseTenMinutesCheck
import com.aos.usecase.bookadd.BooksEntranceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettleUpViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val subscriptionDataStoreUtil: SubscriptionDataStoreUtil,
    private val booksEntranceUseCase : BooksEntranceUseCase
): BaseViewModel() {


    private var _bottom = MutableLiveData<Boolean>(true)
    val bottom: LiveData<Boolean> get() = _bottom

    private var _bookKey = MutableLiveData<String>()
    val bookKey: LiveData<String> get() = _bookKey

    private var _id = MutableLiveData<Long>()
    val id: LiveData<Long> get() = _id

    private var _sharePage = MutableEventFlow<Boolean>()
    val sharePage: EventFlow<Boolean> get() = _sharePage

    // 내역 추가
    private var _clickedAddHistory = MutableEventFlow<String>()
    val clickedAddHistory: EventFlow<String> get() = _clickedAddHistory

    // 구독 만료 여부
    var subscribeExpired = MutableLiveData<Boolean>(false)

    // 구독 유도 팝업 표시 여부
    var subscribePopupShow = MutableLiveData<Boolean>(false)

    // 진입 시 표시되는 팝업인 지
    var subscribePopupEnter = MutableLiveData<Boolean>(true)

    init {
        getSubscribeBenefitChecking()
    }

    fun settingBookKey(id: Long, bk: String){
        viewModelScope.launch {
            _bookKey.value = bk
            _id.value = id
            _sharePage.emit(true)
        }
    }

    fun bottomSee(check : Boolean){
        viewModelScope.launch {
            _bottom.postValue(check)
        }
    }

    // 탭바로 추가할 경우
    fun onClickTabAddHistory() {
        viewModelScope.launch {
            _clickedAddHistory.emit(setTodayDate())
        }
    }

    // 오늘 날짜로 calendar 설정하기
    private fun setTodayDate(): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return date
    }

    // 구독 혜택 받고 있는 지 여부 가져오기
    fun getSubscribeBenefitChecking(){
        viewModelScope.launch(Dispatchers.IO) {
            // 10분 타이머 남은 시간
            val remainTime = prefs.getString("subscribeCheckTenMinutes", "")

            // 구독 만료 여부
            val expiredCheck = subscriptionDataStoreUtil.getSubscribeExpired().first()

            // 10분 지났을 경우 리셋
            if (getAdvertiseTenMinutesCheck(remainTime) < 0)
                prefs.setString("subscribeCheckTenMinutes", "")

            // 구독 만료 여부 업데이트
            subscribeExpired.postValue(expiredCheck)

            // 구독 팝업 표시 여부 업데이트 (구독 만료 O && 타이머 시간이 유효하지 않을 경우)
            subscribePopupShow.postValue(getAdvertiseTenMinutesCheck(remainTime) <= 0 && expiredCheck)
        }
    }

    // 구독 만료 팝업 보이기 (화면 이동 막기)
    fun showSubscribePopupIfNeeded() {
        // 로직이 생길 수 있다면 여기서 처리
        subscribePopupShow.value = true
        subscribePopupEnter.value = false
    }

    fun convertBookCodeToKey(settlementId: Long, bookCode: String) {
        viewModelScope.launch {
            booksEntranceUseCase(bookCode).onSuccess {
                goShareSettlement(settlementId, it.bookKey)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@SettleUpViewModel)))
            }
        }
    }

    private fun goShareSettlement(settlementId: Long?, bookKey: String) {
        if (settlementId != null && bookKey.isNotEmpty()) {
            settingBookKey(settlementId, bookKey)
        } else {
            baseEvent(Event.ShowToast("정산 공유하기 오류가 발생하였습니다."))
        }
    }
}