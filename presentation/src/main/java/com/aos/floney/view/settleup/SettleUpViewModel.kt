package com.aos.floney.view.settleup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.floney.util.getAdvertiseTenMinutesCheck
import com.aos.floney.util.getCurrentDateTimeString
import com.aos.usecase.subscribe.SubscribeBenefitUseCase
import com.aos.usecase.subscribe.SubscribeCheckUseCase
import com.aos.usecase.subscribe.SubscribeUserBenefitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettleUpViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val subscribeBenefitUseCase: SubscribeBenefitUseCase,
    private val SubscribeUserBenefitUseCase: SubscribeUserBenefitUseCase,
    private val subscribeCheckUseCase: SubscribeCheckUseCase
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

    init {
        getSubscribeChecking()
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

    // 구독 여부 가져오기
    fun getSubscribeChecking(){
        viewModelScope.launch(Dispatchers.IO) {
            subscribeCheckUseCase().onSuccess {
                // 구독 안한 상태일 경우, 혜택(가계부, 개인)이 적용되어있는 지 확인
                if(it.isValid)
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

                benefitResult.onSuccess { bookBenefit ->
                    userBenefitResult.onSuccess { userBenefit ->
                        Timber.i("book : ${bookBenefit} user : ${userBenefit}")
                        val subscribeCheckTenMinutes = prefs.getString("subscribeCheckTenMinutes", "")

                        if (getAdvertiseTenMinutesCheck(subscribeCheckTenMinutes) < 0) // 10분 지났을 경우 리셋
                            prefs.setString("subscribeCheckTenMinutes", "")

                        // 구독 만료 여부 업데이트
                        subscribeExpired.postValue(bookBenefit.maxFavorite || bookBenefit.overBookUser || userBenefit.maxBook)

                        // 구독 팝업 표시 여부 업데이트 (구독 만료 O && 타이머 시간이 유효하지 않을 경우)
                        subscribePopupShow.postValue(getAdvertiseTenMinutesCheck(subscribeCheckTenMinutes) <= 0 && subscribeExpired.value == true)
                    }
                }
            } catch (e: Exception) {
                baseEvent(Event.ShowToast(e.message.parseErrorMsg()))
            }
        }
    }

}