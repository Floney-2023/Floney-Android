package com.aos.floney.view.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.floney.util.UtilToken
import com.aos.usecase.signup.SignUpSocialUseCase
import com.aos.usecase.signup.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SignUpInputInfoViewModel @Inject constructor(
    stateHandle: SavedStateHandle,
    private val prefs: SharedPreferenceUtil,
    private val signUpUseCase: SignUpUseCase,
    private val signUpSocialUseCase: SignUpSocialUseCase,
) : BaseViewModel() {

    // 뒤로가기
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back

    // 다음 페이지 이동
    private var _nextPage = MutableEventFlow<Boolean>()
    val nextPage: EventFlow<Boolean> get() = _nextPage

    private var tempEmail = ""
    private var _socialEmail = MutableEventFlow<String>()
    val socialEmail: EventFlow<String> get() = _socialEmail
    var email: LiveData<String> = stateHandle.getLiveData("email")
    var marketing: LiveData<Boolean> = stateHandle.getLiveData("marketing")

    // 비밀번호
    var password = MutableLiveData<String>("")

    // 비밀번호 확인
    var rePassword = MutableLiveData<String>("")

    // 닉네임
    var nickname = MutableLiveData<String>("")
    var socialToken: String? = null
    var socialProvider: String? = null

    // 다음으로 버튼 클릭
    fun onClickNext() {
        if (password.value != "") {
            if (rePassword.value != "") {
                if (password.value.equals(rePassword.value)) {
                    if (isPasswordValid(password.value ?: "")) {
                        if (nickname.value != "") {
                            // 회원가입 시도
                            viewModelScope.launch(Dispatchers.IO) {
                                baseEvent(Event.ShowLoading)
                                // 일반 회원가입인지 소셜 회원가입인지 구분
                                if(socialToken.equals("")) {
                                    // 일반 회원가입
                                    signUpUseCase(email.value ?: "", nickname = nickname.value ?: "", password = password.value ?: "", receiveMarketing = marketing.value ?: false).onSuccess {
                                        // 엑세스 토큰 저장
                                        prefs.setString("accessToken", it.accessToken)
                                        prefs.setString("refreshToken", it.refreshToken)

                                        baseEvent(Event.HideLoading)
                                        _nextPage.emit(true)
                                    }.onFailure {
                                        baseEvent(Event.HideLoading)
                                        baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@SignUpInputInfoViewModel)))
                                    }
                                } else {
                                    val email = if(email.value == "") {
                                        tempEmail
                                    } else {
                                        email.value ?: tempEmail
                                    }
                                    // 소셜 회원가입
                                    signUpSocialUseCase(socialProvider ?: "", socialToken ?: "", email, nickname.value ?: "", marketing.value ?: false).onSuccess {
                                        // 엑세스 토큰 저장
                                        prefs.setString("accessToken", it.accessToken)
                                        prefs.setString("refreshToken", it.refreshToken)
                                        prefs.setString("bookKey", "")

                                        baseEvent(Event.HideLoading)
                                        _nextPage.emit(true)
                                    }.onFailure {
                                        baseEvent(Event.HideLoading)
                                        baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@SignUpInputInfoViewModel)))
                                    }
                                }
                            }
                        } else {
                            // 닉네임 비어있을 경우
                            baseEvent(Event.ShowToastRes(R.string.request_input_nickname))
                        }
                    } else {
                        // 비밀번호 정규식 틀릴 경우
                        baseEvent(Event.ShowToastRes(R.string.password_rule))
                    }
                } else {
                    // 비밀번호 / 비밀번호 확인 일치하지 않을 경우
                    baseEvent(Event.ShowToastRes(R.string.not_match_password))
                }
            } else {
                // 비밀번호 확인 비어있을 경우
                baseEvent(Event.ShowToastRes(R.string.request_input_re_password))
            }
        } else {
            // 비밀번호 비어있을 경우
            baseEvent(Event.ShowToastRes(R.string.request_input_password))
        }
    }

    // 이전 페이지로 이동
    fun onClickPreviousPage() {
        viewModelScope.launch {
            _back.emit(true)
        }
    }

    // 비밀번호 유효성 검사 영문 대소문자 구분 없음, 숫자, 특수문자 8자 이상
    private fun isPasswordValid(password: String): Boolean {
        val passwordRegex =
            Regex("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#\$%^&*])[a-zA-Z0-9!@#\$%^&*]{8,}\$")
        return passwordRegex.matches(password)
    }

    // 소셜 회원가입 정보 저장
    fun setSocialInfo(socialNickname: String, token: String?, provider: String, email: String) {
        nickname.value = socialNickname
        socialToken = token
        socialProvider = provider
        tempEmail = email
        viewModelScope.launch {
            _socialEmail.emit(email)
        }
    }

    fun isSocial(): Boolean {
        return !socialToken.isNullOrEmpty()
    }
}