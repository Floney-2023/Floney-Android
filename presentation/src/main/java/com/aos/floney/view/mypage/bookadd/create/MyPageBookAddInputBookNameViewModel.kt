package com.aos.floney.view.mypage.bookadd.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageBookAddInputBookNameViewModel @Inject constructor(
    private val application: android.app.Application
): BaseViewModel() {

    // 뒤로가기
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back

    // 가계부 프로필 설정하기 화면으로 이동
    private var _nextPage = MutableEventFlow<Boolean>()
    val nextPage: EventFlow<Boolean> get() = _nextPage

    // 가계부 이름
    var bookName = MutableLiveData<String>("")

    // 이전 페이지로 이동
    fun onClickPreviousPage() {
        viewModelScope.launch {
            _back.emit(true)
        }
    }
    // 다음 페이지로 이동 -> 가계부 이름 전달
    fun onClickNextPage(){
        viewModelScope.launch(Dispatchers.IO) {
            if(bookName.value!="")
                _nextPage.emit(true)
            else{
                baseEvent(Event.ShowToast(application.getString(R.string.toast_enter_name)))
            }
        }
    }
}