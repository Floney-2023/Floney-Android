package com.aos.floney.view.mypage.inform.withdraw

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.data.util.SubscriptionDataStoreUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.usecase.withdraw.CheckUserPasswordUseCase
import com.aos.usecase.withdraw.WithdrawUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MyPageInformWithdrawInputPasswordViewModel @Inject constructor(
    stateHandle: SavedStateHandle,
    private val prefs: SharedPreferenceUtil,
    private val subscriptionDataStoreUtil: SubscriptionDataStoreUtil,
    private val withdrawUseCase: WithdrawUseCase,
    private val checkUserPasswordUseCase: CheckUserPasswordUseCase
): BaseViewModel() {


    var type: LiveData<String> = stateHandle.getLiveData("type")
    var reason: LiveData<String?> = stateHandle.getLiveData("reason")

    // 뒤로가기
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back


    // 탈퇴 팝업
    private var _dialogPage = MutableEventFlow<Boolean>()
    val dialogPage: EventFlow<Boolean> get() = _dialogPage

    // 탈퇴 완료
    private var _withdrawPage = MutableEventFlow<Boolean>()
    val withdrawPage: EventFlow<Boolean> get() = _withdrawPage


    // 비밀번호
    var password = MutableLiveData<String>("")

    // 이전 페이지로 이동
    fun onClickPreviousPage()
    {
        viewModelScope.launch {
            _back.emit(true)
        }
    }

    // 탈퇴하기 버튼 클릭
    fun onClickNextButton()
    {
        if(password.value!!.isNotEmpty()) {
            // 비밀번호 넘기기
            viewModelScope.launch(Dispatchers.IO) {
                baseEvent(Event.ShowLoading)
                checkUserPasswordUseCase(password.value!!).onSuccess {
                    baseEvent(Event.HideLoading)
                    _dialogPage.emit(true)
                }.onFailure {
                    baseEvent(Event.HideLoading)
                    baseEvent(Event.ShowToast("비밀번호가 일치하지 않습니다."))
                }
            }
        } else {
            // 비밀번호가 비어 있을 경우
            baseEvent(Event.ShowToastRes(R.string.mypage_main_inform_exit_input_password_toast))
        }

    }
    // 탈퇴 요청
    fun requestWithdraw()
    {
        viewModelScope.launch(Dispatchers.IO) {
            baseEvent(Event.ShowLoading)
            withdrawUseCase(prefs.getString("accessToken",""), type.value!!, reason.value).onSuccess {
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

}