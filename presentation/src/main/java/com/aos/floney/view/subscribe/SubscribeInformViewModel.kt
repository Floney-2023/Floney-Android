package com.aos.floney.view.subscribe

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.Purchase
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.subscribe.UiSubscribeAndroidInfoModel
import com.aos.usecase.subscribe.SubscribeAndroidInfoUseCase
import com.aos.usecase.subscribe.SubscribeAndroidUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SubscribeInformViewModel @Inject constructor(
    private val subscribeAndroidInfoUseCase: SubscribeAndroidInfoUseCase
): BaseViewModel() {

    // 구독 정보
    private var _subscribeInfo = MutableLiveData<UiSubscribeAndroidInfoModel>()
    val subscribeInfo: LiveData<UiSubscribeAndroidInfoModel> get() = _subscribeInfo

    // 구독 해지하기 이동
    private var _resubscribe = MutableEventFlow<Boolean>()
    val resubscribe: EventFlow<Boolean> get() = _resubscribe

    // 구독 해지하기 이동
    private var _unsubscribePage = MutableEventFlow<Boolean>()
    val unsubscribePage: EventFlow<Boolean> get() = _unsubscribePage

    // 구독 정보 화면 나가기
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back

    fun getSubscribeData(){
        viewModelScope.launch(Dispatchers.IO) {
            subscribeAndroidInfoUseCase().onSuccess {
                Timber.i("subscribeInfo : ${it}")
                _subscribeInfo.postValue(it)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@SubscribeInformViewModel)))
            }
        }
    }

    // 구독 정보 화면 나가기
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

    // 다시 구독하기
    fun onClickSubscribeRetry(){
        viewModelScope.launch {
            _resubscribe.emit(true)
        }
    }
}