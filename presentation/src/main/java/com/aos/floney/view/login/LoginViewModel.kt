package com.aos.floney.view.login

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.AuthInterceptor
import com.aos.data.util.CurrencyUtil
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.book.Currency
import com.aos.model.book.CurrencyInform
import com.aos.model.book.getCurrencySymbolByCode
import com.aos.model.user.SocialUserModel
import com.aos.usecase.booksetting.BooksCurrencySearchUseCase
import com.aos.usecase.home.CheckUserBookUseCase
import com.aos.usecase.login.AuthTokenCheckUseCase
import com.aos.usecase.login.LoginUseCase
import com.aos.usecase.login.SocialLoginUseCase
import com.kakao.sdk.template.model.Social
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val application: Application,
    private val prefs: SharedPreferenceUtil,
    private val loginUseCase: LoginUseCase,
    private val checkUserBookUseCase: CheckUserBookUseCase,
    private val authTokenCheckUseCase: AuthTokenCheckUseCase,
    private val socialLoginUseCase: SocialLoginUseCase,
    private val authInterceptor: AuthInterceptor
): BaseViewModel() {

    var email = MutableLiveData<String>("")
    var password = MutableLiveData<String>("")

    // 회원가입 클릭
    private var _clickSignUp = MutableEventFlow<String>()
    val clickSignUp: EventFlow<String> get() = _clickSignUp

    // 가계부 존재 여부
    private var _existBook = MutableEventFlow<Boolean>()
    val existBook: EventFlow<Boolean> get() = _existBook

    // 회원가입 클릭
    private var _clickPasswordFind = MutableEventFlow<Boolean>()
    val clickPasswordFind: EventFlow<Boolean> get() = _clickPasswordFind

    // 카카오 로그인 클릭
    private var _clickKakao = MutableEventFlow<Boolean>()
    val clickKakao: EventFlow<Boolean> get() = _clickKakao

    // 구글 로그인 클릭
    private var _clickGoogle = MutableEventFlow<Boolean>()
    val clickGoogle: EventFlow<Boolean> get() = _clickGoogle

    // 소셜 로그인 임시 값
    private var tempSocialData: SocialUserModel? = null

    // 로그인하기 클릭
    fun onClickLogin() {
        viewModelScope.launch {
            if(email.value!!.isNotEmpty()) {
                if(password.value!!.isNotEmpty()) {
                    baseEvent(Event.ShowLoading)
                    loginUseCase(email.value ?: "", password.value ?: "").onSuccess {
                        baseEvent(Event.HideLoading)

                        prefs.setString("accessToken", it.accessToken)
                        prefs.setString("refreshToken", it.refreshToken)
                        prefs.setString("loginMethod", "normal")

                        authInterceptor.resetSessionExpiredFlag()
                        checkUserBooks()
                    }.onFailure {
                        baseEvent(Event.HideLoading)
                        if(it.message.parseErrorMsg(this@LoginViewModel).equals("잘못된 정보로 로그인에 실패했습니다.")){
                            baseEvent(Event.ShowToast(application.getString(R.string.toast_check_email_password)))
                        } else if(it.message.parseErrorMsg(this@LoginViewModel).equals("해당 이메일로 가입된 유저가 없습니다.")) {
                            baseEvent(Event.ShowToast(application.getString(R.string.toast_no_user_info)))
                        } else{
                            baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@LoginViewModel)))
                        }
                    }
                } else {
                    // 비밀번호가 비어있음
                    baseEvent(Event.ShowToastRes(R.string.request_input_password))
                }
            } else {
                // 이메일이 비어있음
                baseEvent(Event.ShowToastRes(R.string.sign_up_request_email_hint))
            }
        }
    }

    // 유저 가계부 유효 확인
    fun checkUserBooks() {
        viewModelScope.launch {
            checkUserBookUseCase().onSuccess {
                if(it.bookKey != "") {
                    prefs.setString("bookKey", it.bookKey)
                    _existBook.emit(true)
                } else {
                    baseEvent(Event.HideLoading)
                    _existBook.emit(false)
                }
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@LoginViewModel)))
            }
        }
    }

    // 회원 가입 여부 확인
    fun isAuthTokenCheck(provider: String, token: String) {
        viewModelScope.launch {
            authTokenCheckUseCase(provider, token).onSuccess {
                Timber.e("iuiut $it")
                if(it) {
                    // 가입 내역 있음 로그인
                    getSocialLogin(provider, token)
                } else {
                    // 가입 내역 없음 회원가입
                    baseEvent(Event.HideLoading)
                    _clickSignUp.emit("social")
                }
            }.onFailure {
                baseEvent(Event.HideLoading)
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@LoginViewModel)))
            }
        }
    }

    // 소셜 로그인 시도
    private fun getSocialLogin(provider: String, token: String) {
        viewModelScope.launch {
            socialLoginUseCase(provider, token).onSuccess {
                prefs.setString("accessToken", it.accessToken)
                prefs.setString("refreshToken", it.refreshToken)
                prefs.setString("loginMethod", provider)

                authInterceptor.resetSessionExpiredFlag()

                checkUserBooks()
            }.onFailure {
                baseEvent(Event.HideLoading)
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@LoginViewModel)))
            }
        }
    }

    // 소셜 데이터 임시 저장
    fun setSocialTempData(provider: String, token: String, email: String, nickname: String) {
        tempSocialData = SocialUserModel(
            provider, token, email, nickname
        )
    }

    // 소셜 데이터 임시 불러오기
    fun getSocialTempData(): SocialUserModel {
        return tempSocialData!!
    }

    // 비밀번호 찾기 클릭
    fun onClickFindPassword() {
        viewModelScope.launch {
            _clickPasswordFind.emit(true)
        }
    }

    // 회원가입 클릭
    fun onClickSignUp() {
        viewModelScope.launch {
            _clickSignUp.emit("normal")
        }
    }

    // 카카오 로그인 클릭
    fun onClickKakaoLogin() {
        viewModelScope.launch {
            baseEvent(Event.ShowLoading)
            _clickKakao.emit(true)
        }
    }

    // 구글 로그인 클릭
    fun onClickGoogleLogin() {
        viewModelScope.launch {
            baseEvent(Event.ShowLoading)
            _clickGoogle.emit(true)
        }
    }

    // 애플 로그인 클릭
    fun onClickAppleLogin() {

    }

}