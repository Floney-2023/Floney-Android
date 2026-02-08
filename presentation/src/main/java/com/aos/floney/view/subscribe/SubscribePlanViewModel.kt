package com.aos.floney.view.subscribe

import android.app.Activity
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.Purchase
import com.aos.data.util.SharedPreferenceUtil
import com.aos.data.util.SubscriptionDataStoreUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorCode
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.ClickUtil
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.book.UiBookEntranceModel
import com.aos.usecase.bookadd.BooksEntranceUseCase
import com.aos.usecase.bookadd.BooksJoinUseCase
import com.aos.usecase.home.GetBookInfoUseCase
import com.aos.usecase.mypage.MypageSearchUseCase
import com.aos.usecase.subscribe.SubscribeAndroidUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SubscribePlanViewModel @Inject constructor(
    private val app: Application,
    private val subscriptionDataStoreUtil: SubscriptionDataStoreUtil,
    private val subscribeAndroidUseCase: SubscribeAndroidUseCase
): BaseViewModel(), BillingManager.BillingCallback {

    // 구독 하기
    private var _subscribe = MutableEventFlow<Boolean>()
    val subscribe: EventFlow<Boolean> get() = _subscribe

    private val _subscribeChannel = Channel<Boolean>(Channel.CONFLATED)
    val subscribeChannel = _subscribeChannel.receiveAsFlow()

    // 구매내역 복원하기
    private var _subscribeRestore = MutableEventFlow<Boolean>()
    val subscribeRestore: EventFlow<Boolean> get() = _subscribeRestore

    // 구독 해지하기 이동
    private var _servicePage = MutableEventFlow<Boolean>()
    val servicePage: EventFlow<Boolean> get() = _servicePage

    // 구독 정보 화면 나가기
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back

    // 구독 정보 화면 나가기
    private var _subscribeSuccess = MutableEventFlow<Boolean>()
    val subscribeSuccess: EventFlow<Boolean> get() = _subscribeSuccess

    private lateinit var billingManager: BillingManager
    private var pendingPurchase: Purchase? = null

    fun initBillingManager(activity: Activity) {
        if (!::billingManager.isInitialized) {
            billingManager = BillingManager(activity, this)
        } else {
            Timber.d("BillingManager already initialized")
        }
    }

    fun cleanupBillingManager() {
        if (::billingManager.isInitialized) {
            billingManager.endConnection()
        }
    }

    fun startSubscribeConnection(){
        viewModelScope.launch {
            try {
                billingManager.startConnection()
            } catch (e: Exception) {
                Timber.e("Error starting billing connection: ${e.message}")
                baseEvent(Event.ShowToast(app.getString(R.string.toast_payment_connection_failed)))
            }
        }
    }

    override fun onPurchaseTokenReceived(token: String, purchase: Purchase) {
        pendingPurchase = purchase
        sendTokenToServer(token)
    }

    override fun onPurchaseSuccess(checking: Boolean) {
        viewModelScope.launch {
            baseEvent(Event.HideCircleLoading)
            if (checking) {
                subscriptionDataStoreUtil.setUserSubscribe(true)
            }
            _subscribeSuccess.emit(checking)
        }
    }

    override fun onBillingError(errorMsg: String) {
        viewModelScope.launch {
            baseEvent(Event.ShowToast(errorMsg))
        }
    }

    override fun onCleared() {
        super.onCleared()
        cleanupBillingManager()
    }

    private fun sendTokenToServer(purchaseToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            baseEvent(Event.ShowCircleLoading)
            subscribeAndroidUseCase(purchaseToken).onSuccess {
                Timber.d("subscribeAndroidUseCase onSuccess")
                pendingPurchase?.let { billingManager.acknowledgePurchase(it) } // acknowledgePurchase 호출
            }.onFailure {
                baseEvent(Event.HideCircleLoading)
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@SubscribePlanViewModel)))
            }
        }
    }

    // 구독 정보 화면 나가기
    fun onClickedExit(){
        viewModelScope.launch {
            _back.emit(true)
        }
    }

    // 구독 내역 복원하기
    fun onClickPlanRestore(){
        viewModelScope.launch {
            _subscribeRestore.emit(true)
        }
    }

    // 구독 하기
    fun onClickSubscribe() {
        ClickUtil.debounceClick {
            viewModelScope.launch {
                _subscribeChannel.send(true)
            }
        }
    }


    // 서비스 이용 약관
    fun onClickService(){
        viewModelScope.launch {
            _servicePage.emit(true)
        }
    }
}