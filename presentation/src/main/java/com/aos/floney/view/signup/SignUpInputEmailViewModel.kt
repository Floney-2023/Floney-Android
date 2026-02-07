package com.aos.floney.view.signup

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.usecase.signup.SendEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpInputEmailViewModel @Inject constructor(
    stateHandle: SavedStateHandle,
    private val sendEmailUseCase: SendEmailUseCase
): BaseViewModel() {

    var marketing: LiveData<Boolean> = stateHandle.getLiveData("marketing")

    // 뒤로가기
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back
    private var _nextPage = MutableEventFlow<Boolean>()
    val nextPage: EventFlow<Boolean> get() = _nextPage

    // 이메일
    var email = MutableLiveData<String>("")

    // 이메일 전송
    fun onClickSendEmail() {
        if(email.value!!.isNotEmpty()) {
            if(isEmailValid()) {
                // 이메일 전송
                viewModelScope.launch(Dispatchers.IO) {
                    baseEvent(Event.ShowLoading)

                    sendEmailUseCase(email.value!!).onSuccess {
                        // 전송 성공
                        baseEvent(Event.HideLoading)
                        _nextPage.emit(true)
                    }.onFailure {
                        baseEvent(Event.HideLoading)
                        if(it.message.parseErrorMsg(this@SignUpInputEmailViewModel) == "이미 존재하는 유저입니다") {
                            baseEvent(Event.ShowToastRes(R.string.sign_up_exist_email))
                        } else {
                            baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@SignUpInputEmailViewModel)))
                        }
                    }
                }
            } else {
                // 이메일이 유효하지 않은 형태일 경우
                baseEvent(Event.ShowToastRes(R.string.sign_up_request_valid_email))
            }
        } else {
            // 이메일이 비어 있을 경우
            baseEvent(Event.ShowToastRes(R.string.sign_up_request_email))
        }
    }

    // 이전 페이지로 이동
    fun onClickPreviousPage() {
        viewModelScope.launch {
            _back.emit(true)
        }
    }

    // 이메일 유효성 체크
    private fun isEmailValid(): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email.value!!).matches()
    }

    fun setEmail(socialEmail: String) {
        email.value = socialEmail
    }
}