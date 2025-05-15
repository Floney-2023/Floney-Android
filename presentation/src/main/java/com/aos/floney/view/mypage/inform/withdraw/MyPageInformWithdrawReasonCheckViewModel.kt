package com.aos.floney.view.mypage.inform.withdraw

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.data.util.SubscriptionDataStoreUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.usecase.subscribe.SubscribeAndroidInfoUseCase
import com.aos.usecase.subscribe.SubscribeCheckUseCase
import com.aos.usecase.withdraw.WithdrawUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MyPageInformWithdrawReasonCheckViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val subscriptionDataStoreUtil: SubscriptionDataStoreUtil,
    private val withdrawUseCase: WithdrawUseCase,
    private val subscribeAndroidInfoUseCase: SubscribeAndroidInfoUseCase
) : BaseViewModel() {

    // 뒤로가기
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back


    // 탈퇴하기
    private var _nextPage = MutableEventFlow<String>()
    val nextPage: EventFlow<String> get() = _nextPage


    // 탈퇴 사유 직접 입력
    var directInputText = MutableLiveData<String>("")


    // 어떻게 사용하는 지 모르겠어요 체크
    private var _howToUseTerms = MutableLiveData<Boolean>(false)
    val howToUseTerms: LiveData<Boolean> get() = _howToUseTerms

    // 비싸요 체크
    private var _expensiveTerms = MutableLiveData<Boolean>(false)
    val expensiveTerms: LiveData<Boolean> get() = _expensiveTerms

    // 자주 사용 안해요 체크
    private var _noAlreadyTerms = MutableLiveData<Boolean>(false)
    val noAlreadyTerms: LiveData<Boolean> get() = _noAlreadyTerms

    // 다시 가입 가입할 거예요 체크
    private var _reLoginTerms = MutableLiveData<Boolean>(false)
    val reLoginTerms: LiveData<Boolean> get() = _reLoginTerms

    // 직접 입력하기 체크
    private var _directInputTerms = MutableLiveData<Boolean>(false)
    val directInputTerms: LiveData<Boolean> get() = _directInputTerms

    // 탈퇴 완료
    private var _withdrawPage = MutableEventFlow<Boolean>()
    val withdrawPage: EventFlow<Boolean> get() = _withdrawPage

    // 구독 결제 여부
    var subscribeCheck = MutableLiveData<Boolean>(false)

    init {
        // 구독 여부를 가져옵니다.
        getSubscribeChecking()

        // 각 체크박스의 상태를 변경할 때 다른 체크박스의 상태를 변경합니다.
        _howToUseTerms.observeForever {
            if (it) {
                _expensiveTerms.postValue(false)
                _noAlreadyTerms.postValue(false)
                _reLoginTerms.postValue(false)
                _directInputTerms.postValue(false)
            }
        }
        _expensiveTerms.observeForever {
            if (it) {
                _howToUseTerms.postValue(false)
                _noAlreadyTerms.postValue(false)
                _reLoginTerms.postValue(false)
                _directInputTerms.postValue(false)
            }
        }
        _noAlreadyTerms.observeForever {
            if (it) {
                _howToUseTerms.postValue(false)
                _expensiveTerms.postValue(false)
                _reLoginTerms.postValue(false)
                _directInputTerms.postValue(false)
            }
        }
        _reLoginTerms.observeForever {
            if (it) {
                _howToUseTerms.postValue(false)
                _expensiveTerms.postValue(false)
                _noAlreadyTerms.postValue(false)
                _directInputTerms.postValue(false)
            }
        }
        _directInputTerms.observeForever {
            if (it) {
                _howToUseTerms.postValue(false)
                _expensiveTerms.postValue(false)
                _noAlreadyTerms.postValue(false)
                _reLoginTerms.postValue(false)
            }
        }
    }

    // 이전 페이지로 이동
    fun onClickPreviousPage() {
        viewModelScope.launch {
            _back.emit(true)
        }
    }

    // 어떻게 사용하는 지 모르겠어요 체크
    fun onClickHowToUseTerms() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _howToUseTerms.postValue(!_howToUseTerms.value!!)
            }
        }
    }

    // 구독료 비싸요 체크
    fun onClickExpensiveTerms() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _expensiveTerms.postValue(!_expensiveTerms.value!!)
            }
        }
    }

    // 자주 사용 안해요 체크
    fun onClickNoAlreadyTerms() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _noAlreadyTerms.postValue(!_noAlreadyTerms.value!!)
            }
        }
    }

    // 다시 가입할 거예요 체크
    fun onClickReLoginTerms() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _reLoginTerms.postValue(!_reLoginTerms.value!!)
            }
        }
    }

    // 직접 입력하기 체크
    fun onClickDirectInputTerms() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _directInputTerms.postValue(!_directInputTerms.value!!)
            }
        }
    }

    // 탈퇴하기 버튼 클릭
    fun onClickExitButton() {
        val checkedTermType = getCheckedTermType()
        if (checkedTermType != null) {
            if (checkedTermType.equals("OTHER") && directInputText.value!! == "") {
                // 직접 입력 텍스트가 비어 있는 경우 Toast를 표시합니다.
                baseEvent(Event.ShowToastRes(R.string.mypage_main_inform_exit_blank_toast))
            } else {
                viewModelScope.launch {
                    _nextPage.emit(checkedTermType)
                }
            }
        } else {
            baseEvent(Event.ShowToastRes(R.string.mypage_main_inform_exit_check_toast))
        }
    }

    private fun getCheckedTermType(): String? {
        // terms value 리스트
        val termsValues = listOf(
            _howToUseTerms.value,
            _expensiveTerms.value,
            _noAlreadyTerms.value,
            _reLoginTerms.value,
            _directInputTerms.value
        )

        // 선택된 terms value 개수 카운트
        val checkedCount = termsValues.count { it == true }

        // 선택된 terms value가 1개인 경우에만 처리
        if (checkedCount == 1) {
            // 선택된 terms value에 따라 문자열 반환
            return when {
                _howToUseTerms.value == true -> "NOT_KNOW_HOW_TO_USE"
                _expensiveTerms.value == true -> "EXPENSIVE"
                _noAlreadyTerms.value == true -> "NOT_USE_OFTEN"
                _reLoginTerms.value == true -> "WILL_SIGNUP_AGAIN"
                _directInputTerms.value == true -> "OTHER"
                else -> null
            }
        } else {
            return null
        }
    }
    // 탈퇴 요청
    fun requestWithdraw(reasonType: String, reason: String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            baseEvent(Event.ShowLoading)
            withdrawUseCase(prefs.getString("accessToken",""), reasonType, reason).onSuccess {
                baseEvent(Event.HideLoading)

                prefs.setString("bookKey","")
                prefs.setString("accessToken","")
                subscriptionDataStoreUtil.initAllToFalse()

                _withdrawPage.emit(true)
            }.onFailure {
                baseEvent(Event.HideLoading)
                baseEvent(Event.ShowToast("알 수 없는 오류입니다. 다시 시도해 주세요."))
            }
        }
    }

    // 구독 여부 가져오기
    fun getSubscribeChecking(){
        viewModelScope.launch(Dispatchers.IO) {
            subscribeAndroidInfoUseCase().onSuccess {
                subscribeCheck.postValue(it.autoRenewing)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
            }
        }
    }
}