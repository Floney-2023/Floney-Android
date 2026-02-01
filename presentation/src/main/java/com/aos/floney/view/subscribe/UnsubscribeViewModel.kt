package com.aos.floney.view.subscribe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorCode
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.book.UiBookEntranceModel
import com.aos.usecase.bookadd.BooksEntranceUseCase
import com.aos.usecase.bookadd.BooksJoinUseCase
import com.aos.usecase.home.GetBookInfoUseCase
import com.aos.usecase.mypage.MypageSearchUseCase
import com.aos.usecase.subscribe.SubscribeAndroidInfoUseCase
import com.aos.usecase.subscribe.SubscribeCheckUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UnsubscribeViewModel @Inject constructor(
    private val subscribeAndroidInfoUseCase: SubscribeAndroidInfoUseCase,
    private val subscribeCheckUseCase: SubscribeCheckUseCase
): BaseViewModel() {


    // 구독 유지하기
    private var _resubscribe = MutableEventFlow<Boolean>()
    val resubscribe: EventFlow<Boolean> get() = _resubscribe

    // 구독 해지하기 이동
    private var _unsubscribePage = MutableEventFlow<Boolean>()
    val unsubscribePage: EventFlow<Boolean> get() = _unsubscribePage

    // 구독 정보 화면 나가기
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back

    // 구독 여부
    private var _subscribePage = MutableEventFlow<Boolean>()
    val subscribePage: EventFlow<Boolean> get() = _subscribePage

    // 구독 여부 가져오기
    fun getSubscribeStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            subscribeAndroidInfoUseCase().onSuccess {
                Timber.i("subscribeInfo : ${it}")
                _subscribePage.emit(it.autoRenewing)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
            }
        }
    }

    // 구독 해지하기 화면 나가기
    fun onClickedExit(){
        viewModelScope.launch {
            _back.emit(true)
        }
    }

    // 구독 해지하기
    fun onClickUnsubscribe(){
        viewModelScope.launch {
            _unsubscribePage.emit(true)
        }
    }

    // 구독 유지하기
    fun onClickSubscribeStay(){
        viewModelScope.launch {
            _resubscribe.emit(true)
        }
    }
}