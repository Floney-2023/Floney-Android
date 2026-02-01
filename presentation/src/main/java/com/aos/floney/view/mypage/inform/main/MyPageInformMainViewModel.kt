package com.aos.floney.view.mypage.inform.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.data.util.SubscriptionDataStoreUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.usecase.logout.LogoutUseCase
import com.aos.usecase.mypage.MypageSearchUseCase
import com.aos.usecase.mypage.NicknameChangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageInformMainViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val subscriptionDataStoreUtil : SubscriptionDataStoreUtil,
    private val nicknameChangeUseCase : NicknameChangeUseCase,
    private val mypageSearchUseCase : MypageSearchUseCase,
    private val logoutUseCase : LogoutUseCase
): BaseViewModel() {

    // 뒤로가기
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back

    // 바뀔 닉네임
    var nickName = MutableLiveData<String?>("")

    // hint 닉네임
    var hintName = MutableLiveData<String?>("")

    // provider
    var provider = MutableLiveData<String?>("")

    // 프로필 이미지 변경 페이지 이동
    private var _profileChangePage = MutableEventFlow<Boolean>()
    val profileChangePage: EventFlow<Boolean> get() = _profileChangePage

    // 비밀번호 변경 페이지 이동
    private var _pwChangePage = MutableEventFlow<Boolean>()
    val pwChangePage: EventFlow<Boolean> get() = _pwChangePage

    // 로그아웃
    private var _logOutPage = MutableEventFlow<Boolean>()
    val logOutPage: EventFlow<Boolean> get() = _logOutPage


    // 로그아웃
    private var _logOutAlert = MutableEventFlow<Boolean>()
    val logOutAlert: EventFlow<Boolean> get() = _logOutAlert

    // 회원탈퇴
    private var _withDrawalPage = MutableEventFlow<Boolean>()
    val withDrawalPage: EventFlow<Boolean> get() = _withDrawalPage

    init {
        searchMypageItems()
    }

    // Provider 설정
    fun searchMypageItems(){
        viewModelScope.launch(Dispatchers.IO) {
            mypageSearchUseCase().onSuccess {
                hintName.postValue(it.nickname)
                provider.postValue(it.provider)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
            }
        }
    }
    // 이전 페이지로 이동
    fun onClickPreviousPage()
    {
        viewModelScope.launch {
            _back.emit(true)
        }
    }
    // 닉네임 변경 버튼 클릭
    fun onClickNicknameChange()
    {
        if(nickName.value!!.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                baseEvent(Event.ShowLoading)
                nicknameChangeUseCase(nickname = nickName.value ?: "").onSuccess {
                    // 닉네임 변경 성공
                    baseEvent(Event.ShowSuccessToastRes(R.string.mypage_main_inform_nickname_request_success))
                    baseEvent(Event.HideLoading)
                }.onFailure {
                    baseEvent(Event.HideLoading)
                    baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@MyPageInformMainViewModel)))
                }
            }
        }else {
            // 비밀번호 비어있을 경우
            baseEvent(Event.ShowToastRes(R.string.mypage_main_inform_nickname_request))
        }

    }
    // 프로필 변경 페이지 이동
    fun onClickProfileImgChange()
    {
        viewModelScope.launch {
            _profileChangePage.emit(true)
        }
    }
    // 비밀번호 변경
    fun onClickPasswordChange()
    {
        viewModelScope.launch {
            _pwChangePage.emit(true)
        }
    }
    // 로그아웃
    fun onClickLogOut()
    {
        viewModelScope.launch {
            _logOutAlert.emit(true)
        }

    }
    fun onLogOut()
    {
        viewModelScope.launch {
            if(prefs.getString("accessToken","").isNotEmpty()) {
                baseEvent(Event.ShowLoading)
                logoutUseCase(prefs.getString("accessToken","")).onSuccess {
                    prefs.setString("accessToken", "")
                    prefs.setString("refreshToken", "")
                    prefs.setString("bookKey","")
                    prefs.getString("subscribeCheckTenMinutes", "")
                    subscriptionDataStoreUtil.initAllToFalse()

                    _logOutPage.emit(true)
                }.onFailure {
                    baseEvent(Event.HideLoading)
                    baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@MyPageInformMainViewModel)))
                }
            }
        }
    }
    // 회원탈퇴
    fun onClickWithdrawal()
    {
        viewModelScope.launch {
            _withDrawalPage.emit(true)
        }
    }

}