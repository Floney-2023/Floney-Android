package com.aos.floney.view.analyze

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.data.util.SubscriptionDataStoreUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.floney.util.getAdvertiseTenMinutesCheck
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AnalyzeViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val subscriptionDataStoreUtil: SubscriptionDataStoreUtil
): BaseViewModel() {

    // 지출, 수입, 예산, 자산
    private var _flow = MutableLiveData<String>("지출")
    val flow: LiveData<String> get() = _flow

    private var _onClickSetBudget = MutableLiveData<Boolean>()
    val onClickSetBudget : LiveData<Boolean> get() = _onClickSetBudget

    private var _onClickChoiceDate = MutableEventFlow<String>()
    val onClickChoiceDate: EventFlow<String> get() = _onClickChoiceDate

    private var _onChangedDate = MutableEventFlow<String>()
    val onChangedDate: EventFlow<String> get() = _onChangedDate

    // 날짜 데이터
    private val _calendar = MutableStateFlow<Calendar>(Calendar.getInstance())

    // 표시 중인 날짜
    private var _showDate = MutableLiveData<String>()
    val showDate: LiveData<String> get() = _showDate

    // 내역추가
    private var _clickedAddHistory = MutableEventFlow<String>()
    val clickedAddHistory: EventFlow<String> get() = _clickedAddHistory

    // 구독 적용 여부
    var subscribeActive = false

    // 구독 만료됨 & 혜택 적용 여부
    var subscribeExpired = false

    // 구독 유도 팝업 표시 여부
    var subscribePopupShow = MutableLiveData<Boolean>(false)

    init {
        getFormatDateMonth()
        getSubscribeChecking()
        getSubscribeBenefitChecking()
    }

    // 지출, 수입, 이체 클릭
    fun onClickFlow(type: String) {
        if (flow.value == type) return
        _flow.postValue(type)
    }

    // 예산 설정하기 클릭
    fun onClickSetBudget(flag: Boolean) {
        _onClickSetBudget.postValue(flag)
    }

    // 날짜 선택 버튼 클릭
    fun onClickChoiceDate() {
        viewModelScope.launch {
            _onClickChoiceDate.emit(getFormatDate())
        }
    }

    // 이전 달 버튼 클릭
    fun onClickPreviousMonth() {
        updateCalendarMonth(-1)
    }

    // 다음 달 버튼 클릭
    fun onClickNextMonth() {
        updateCalendarMonth(1)
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

    // 날짜 포멧 월만 결과 가져오기
    private fun getFormatDateMonth() {
        val date = SimpleDateFormat("M월", Locale.getDefault()).format(_calendar.value.time)
        _showDate.postValue(date)

        viewModelScope.launch {
            _onChangedDate.emit(getFormatDate())
        }
    }

    // 날짜 포멧 전체 결과 가져오기
    fun getFormatDate(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(_calendar.value.time) + "-01"
    }

    // 캘린더 값 변경
    fun updateCalendarClickedItem(year: Int, month: Int) {
        _calendar.value.set(Calendar.YEAR, year)
        _calendar.value.set(Calendar.MONTH, month - 1)
        _calendar.value.set(Calendar.DATE, 1)

        // 월 업데이트
        getFormatDateMonth()
    }

    // 캘린더 값 변경
    private fun updateCalendarMonth(value: Int) {
        _calendar.value.set(Calendar.DAY_OF_MONTH, 1)
        _calendar.value.add(Calendar.MONTH, value)

        // 월 업데이트
        getFormatDateMonth()
    }

    // 구독 만료 팝업 보이기
    fun showSubscribePopupIfNeeded() {
        // 로직이 생길 수 있다면 여기서 처리
        subscribePopupShow.value = true
    }


    // 가계부 구독 여부 가져오기
    fun getSubscribeChecking(){
        viewModelScope.launch {
            subscribeActive = subscriptionDataStoreUtil.getBookSubscribe().first()
        }
    }

    // 구독 만료 여부 가져오기
    fun getSubscribeBenefitChecking(){
        viewModelScope.launch(Dispatchers.IO) {
            // 둘 다 적용 중인 상태라면 확인하지 않는다.
            if (subscriptionDataStoreUtil.getBookSubscribe().first() && subscriptionDataStoreUtil.getUserSubscribe().first())
                return@launch

            // 10분 타이머 남은 시간
            val remainTime = prefs.getString("subscribeCheckTenMinutes", "")

            // 10분 지났을 경우 리셋
            if (getAdvertiseTenMinutesCheck(remainTime) < 0)
                prefs.setString("subscribeCheckTenMinutes", "")

            // 구독 적용 중 아닌데 혜택 있는 경우
            subscribeExpired = subscriptionDataStoreUtil.getSubscribeExpired().first()

            // 구독 팝업 표시 여부 업데이트 (구독 만료 O && 타이머 시간이 유효하지 않을 경우)
            subscribePopupShow.postValue(getAdvertiseTenMinutesCheck(remainTime) <= 0 && subscribeExpired)

        }
    }
}