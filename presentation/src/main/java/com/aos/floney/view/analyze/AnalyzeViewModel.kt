package com.aos.floney.view.analyze

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.usecase.subscribe.SubscribeBenefitUseCase
import com.aos.usecase.subscribe.SubscribeCheckUseCase
import com.aos.usecase.subscribe.SubscribeUserBenefitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AnalyzeViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val subscribeBenefitUseCase: SubscribeBenefitUseCase,
    private val SubscribeUserBenefitUseCase: SubscribeUserBenefitUseCase,
    private val subscribeCheckUseCase: SubscribeCheckUseCase
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

    // 구독 만료 내역
    var subscribeExpired = MutableLiveData<Boolean>(false)

    init {
        getFormatDateMonth()
        getSubscribeChecking()
    }

    // 지출, 수입, 이체 클릭
    fun onClickFlow(type: String) {
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


    // 구독 여부 가져오기
    fun getSubscribeChecking(){
        viewModelScope.launch(Dispatchers.IO) {
            subscribeCheckUseCase().onSuccess {
                // 구독 안한 상태일 경우, 혜택(가계부, 개인)이 적용되어있는 지 확인
                if(!it.isValid)
                    getSubscribeBenefitChecking()
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
            }
        }
    }

    // 구독 혜택 받고 있는 지 여부 가져오기
    fun getSubscribeBenefitChecking(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bookKey = prefs.getString("bookKey", "")

                // 가계부 혜택 확인
                val benefitResult = subscribeBenefitUseCase(bookKey)
                benefitResult.onFailure {
                    baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
                    return@launch // 실패 시 작업 종료
                }

                // 유저 혜택 확인
                val userBenefitResult = SubscribeUserBenefitUseCase()
                userBenefitResult.onFailure {
                    baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
                    return@launch // 실패 시 작업 종료
                }

                // 두 작업이 모두 성공한 경우 처리
                benefitResult.onSuccess { bookBenefit ->
                    userBenefitResult.onSuccess { userBenefit ->
                        Timber.i("book : ${bookBenefit} user : ${userBenefit}")
                        subscribeExpired.postValue(bookBenefit.maxFavorite || bookBenefit.overBookUser || userBenefit.maxBook)
                    }
                }
            } catch (e: Exception) {
                // 코루틴 실행 중 발생한 예외 처리
                baseEvent(Event.ShowToast(e.message.parseErrorMsg()))
            }
        }
    }
}