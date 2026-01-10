package com.aos.floney.view.book.entrance

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.CommonUtil
import com.aos.data.util.SharedPreferenceUtil
import com.aos.data.util.SubscriptionDataStoreUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorCode
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.book.UiBookEntranceModel
import com.aos.model.home.UiBookInfoModel
import com.aos.usecase.bookadd.BooksEntranceUseCase
import com.aos.usecase.bookadd.BooksJoinUseCase
import com.aos.usecase.home.GetBookInfoUseCase
import com.aos.usecase.mypage.MypageSearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookEntranceViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val subscriptionDataStoreUtil: SubscriptionDataStoreUtil,
    private val booksJoinUseCase: BooksJoinUseCase,
    private val mypageSearchUseCase: MypageSearchUseCase,
    private val booksEntranceUseCase : BooksEntranceUseCase,
    private val getBookInfoUseCase: GetBookInfoUseCase
): BaseViewModel() {

    // 가계부 이름
    var inviteCode = MutableLiveData<String>("")


    private var _bookInfo = MutableLiveData<UiBookEntranceModel>()
    val bookInfo: LiveData<UiBookEntranceModel> get() = _bookInfo

    // 가계부 코드 입력하는 화면으로 이동
    private var _codeInputPage = MutableEventFlow<Boolean>()
    val codeInputPage: EventFlow<Boolean> get() = _codeInputPage

    // 가계부 초대 받기 성공, 이동
    private var _newBookCreatePage = MutableEventFlow<Boolean>()
    val newBookCreatePage: EventFlow<Boolean> get() = _newBookCreatePage

    // 코드 복사
    private var _inviteCodeCopy = MutableEventFlow<Boolean>()
    val inviteCodeCopy: EventFlow<Boolean> get() = _inviteCodeCopy

    // 화면 나가기
    private var _inviteCodeExit = MutableEventFlow<Boolean>()
    val inviteCodeExit: EventFlow<Boolean> get() = _inviteCodeExit

    fun setinviteCode(code: String){
        viewModelScope.launch {
            booksEntranceUseCase(code).onSuccess {
                inviteCode.postValue(code)
                _bookInfo.postValue(it)

            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@BookEntranceViewModel)))
            }
        }
    }
    fun onClickInviteCodeCopy(){
        viewModelScope.launch {
            _inviteCodeCopy.emit(true)
        }
    }

    fun onClickEntrance() {
        viewModelScope.launch(Dispatchers.IO) {
            if (canCreateMoreBooks()) {
                bookEntrance()
            } else {
                baseEvent(Event.ShowToastRes(R.string.book_entrance_over_alert))
            }
        }
    }

    fun bookEntrance()
    {
        if(inviteCode.value!!.isNotEmpty()) {
            // 참여 코드 전송
            viewModelScope.launch(Dispatchers.IO) {
                baseEvent(Event.ShowLoading)

                booksJoinUseCase(inviteCode.value!!).onSuccess {
                    // 참여 완료, 참여 가계부 키 설정
                    prefs.setString("bookKey", it.bookKey)
                    delay(1)
                    baseEvent(Event.HideLoading)

                    _newBookCreatePage.emit(true)
                }.onFailure {
                    baseEvent(Event.HideLoading)

                    val errorCode = it.message.parseErrorCode()

                    val message = when (errorCode) {
                        "B008" -> "이미 참여한 가계부 입니다."
                        "B002" -> "이미 사용자가 가득 찬 가계부 입니다."
                        "B001" -> "존재하지 않는 가계부 입니다."
                        else -> "알 수 없는 오류입니다. 다시 시도해 주세요."
                    }
                    baseEvent(Event.ShowToast(message))

                }
            }
        } else {
            // 참여 코드가 비어 있을 경우
            baseEvent(Event.ShowToastRes(R.string.book_add_invite_check_request_empty_code))
        }
    }
    fun onClickedSkipBtn(){
        viewModelScope.launch {
            getBookInfoUseCase(prefs.getString("bookKey","")).onSuccess {
                _inviteCodeExit.emit(true)
            }.onFailure {
                _inviteCodeExit.emit(false)
            }
        }
    }

    fun onClickCodeInput() {
        viewModelScope.launch(Dispatchers.IO) {
            if (canCreateMoreBooks()) {
                _codeInputPage.emit(true)
            } else {
                baseEvent(Event.ShowToastRes(R.string.book_entrance_over_alert))
            }
        }
    }

    private suspend fun canCreateMoreBooks(): Boolean {
        val bookLimit = if (subscriptionDataStoreUtil.getUserSubscribe().first()) 4 else 2
        val result = mypageSearchUseCase()
        return result.getOrNull()?.myBooks?.size?.let { it < bookLimit } ?: false
    }
}