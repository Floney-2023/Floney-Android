package com.aos.floney.view.mypage.bookadd.codeinput

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorCode
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.usecase.bookadd.BooksJoinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageBookAddInviteCheckViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val booksJoinUseCase: BooksJoinUseCase
): BaseViewModel() {
    // 뒤로가기
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back

    // 입력한 참여 코드
    var code = MutableLiveData<String>("")


    // 초대받은 가계부 코드 입력한 후, 이동
    private var _codeInputCompletePage = MutableEventFlow<Boolean>()
    val codeInputCompletePage: EventFlow<Boolean> get() = _codeInputCompletePage

    // 이전 페이지로 이동
    fun onClickPreviousPage()
    {
        viewModelScope.launch {
            _back.emit(true)
        }
    }

    fun onClickInputComplete() {
        if(code.value!!.isNotEmpty()) {
                // 참여 코드 전송
                viewModelScope.launch(Dispatchers.IO) {
                    baseEvent(Event.ShowLoading)
                    booksJoinUseCase(code.value!!).onSuccess {
                        // 참여 완료, 참여 가계부 키 설정
                        prefs.setString("bookKey", it.bookKey)
                        delay(1000)
                        baseEvent(Event.HideLoading)
                        _codeInputCompletePage.emit(true)
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

}