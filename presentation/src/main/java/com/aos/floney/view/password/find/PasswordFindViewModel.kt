package com.aos.floney.view.password.find

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorCode
import com.aos.floney.ext.parseErrorKey
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.usecase.password.SendTempPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordFindViewModel @Inject constructor(
    private val app: Application,
    private val sendTempPasswordUseCase: SendTempPasswordUseCase
): BaseViewModel() {

    // 이메일
    var email = MutableLiveData<String>("")
    private var _nextPage = MutableEventFlow<Boolean>()
    val nextPage: EventFlow<Boolean> get() = _nextPage
    var _previousPage = MutableEventFlow<Boolean>()
    val previousPage: EventFlow<Boolean> get() = _previousPage
    var _showSendDialog = MutableEventFlow<Boolean>()
    val showSendDialog: EventFlow<Boolean> get() = _showSendDialog

    // 임시 비밀번호 보내기
    fun onClickSendTempPassword() {
        if(email.value!!.isNotEmpty()) {
            if(isEmailValid()) {
                // 이메일 전송
                viewModelScope.launch(Dispatchers.IO) {
                    baseEvent(Event.ShowLoading)

                    sendTempPasswordUseCase(email.value!!).onSuccess {
                        // 전송 성공
                        baseEvent(Event.HideLoading)
                        _showSendDialog.emit(true)
                    }.onFailure {
                        baseEvent(Event.HideLoading)

                        val errorCode = it.message.parseErrorCode()

                        val message = when (errorCode) {
                            "U008" -> app.getString(R.string.toast_no_user_with_email)
                            "U021" -> {
                                val provider = it.message.parseErrorKey(key = "provider")
                                app.getString(R.string.mypage_main_inform_pwchange_u021_toast, provider)
                            }
                            else -> app.getString(R.string.toast_error_unknown)
                        }

                        baseEvent(Event.ShowToast(message))
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

    fun onClickedPreviousBtn() {
        viewModelScope.launch {
            _previousPage.emit(true)
        }
    }


    // 이메일 유효성 체크
    private fun isEmailValid(): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email.value!!).matches()
    }
}