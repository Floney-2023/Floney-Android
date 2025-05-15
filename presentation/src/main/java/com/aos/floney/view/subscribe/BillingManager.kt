package com.aos.floney.view.subscribe

import android.app.Activity
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.aos.floney.BuildConfig
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.view.common.ErrorToastDialog
import com.aos.usecase.bookadd.BooksJoinUseCase
import com.aos.usecase.subscribe.SubscribeAndroidUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class BillingManager(
    private val activity: Activity,
    private val billingCallback: BillingCallback
) {

    interface BillingCallback {
        fun onPurchaseTokenReceived(token: String, purchase: Purchase)
        fun onPurchaseSuccess(checking: Boolean)
        fun onBillingError(errorMsg: String)
    }

    private var billingClient: BillingClient

    init {
        billingClient = BillingClient.newBuilder(activity)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts()
                    .enablePrepaidPlans()
                    .build()
            )
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase) // 구매 처리
                    }
                } else {
                    // 결제 실패 처리
                    Timber.e("Purchase failed: ${billingResult.debugMessage}")
                    billingCallback.onBillingError("결제가 취소되었습니다.")
                }
            }
            .build()
    }

    // 구매 정보 처리
    private fun handlePurchase(purchase: Purchase) {
        // 구매 토큰을 서버로 보내기 전에 구매 상태를 확인해야 함
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            // 구매가 완료된 상태에서만 처리
            val purchaseToken = purchase.purchaseToken
            Timber.i("Purchase successful, token: $purchaseToken")

            // 토큰을 서버로 전송하여 검증
            billingCallback.onPurchaseTokenReceived(purchaseToken, purchase)
        }
    }

    // 구매 확인 (Acknowledgement)
    fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Timber.i("Purchase acknowledged")
                billingCallback.onPurchaseSuccess(true)
            } else {
                Timber.e("Failed to acknowledge purchase: ${billingResult.debugMessage}")
                billingCallback.onPurchaseSuccess(false)
            }
        }
    }

    private var connectionRetryCount = 0
    private val MAX_RETRY_ATTEMPTS = 3

    private fun retryBillingConnection() {
        if (connectionRetryCount < MAX_RETRY_ATTEMPTS) {
            connectionRetryCount++
            Timber.d("Retrying billing connection, attempt: $connectionRetryCount")
            startConnection()
        } else {
            Timber.e("Failed to connect to billing service after $MAX_RETRY_ATTEMPTS attempts")
            // Notify the user
            billingCallback.onPurchaseSuccess(false)
        }
    }

    fun startConnection() {
        // If already connected, proceed with subscription query
        if (billingClient.isReady) {
            Timber.d("Billing client is already connected")
            querySubscriptionDetails()
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Google Play 서비스 연결이 끊어진 경우 처리
                Timber.e("Billing service disconnected. Attempting to reconnect...")
                // Try to reconnect
                retryBillingConnection()
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // 구독 상품 로드 또는 구매 가능 처리
                    Timber.e("checking 2 : onBillingSetupFinished")
                    querySubscriptionDetails()
                }
            }
        })
    }

    fun querySubscriptionDetails() {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BuildConfig.GOOGLE_PRODUCT_SUBSCRIBE_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            ).build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !productDetailsList.isNullOrEmpty()) {
                // SKU 세부 사항 로드 완료
                val productDetails = productDetailsList[0] // 첫 번째 상품 정보 사용
                // 구매 플로우 실행
                Timber.e("checking 3 : productDetails ${billingResult.responseCode}, message: ${productDetailsList}")
                launchPurchaseFlow(productDetails)

            } else {
                // 오류 처리
                Timber.e("checking 4 : Error code: ${billingResult.responseCode}, message: ${productDetailsList}")
                billingCallback.onBillingError("거래 정보를 불러오는 데에 오류가 발생하였습니다.")
            }
        }
    }

    fun launchPurchaseFlow(productDetails: ProductDetails) {
        if (!billingClient.isReady) {
            Timber.e("Billing client is not ready")
            billingCallback.onPurchaseSuccess(false)
            return
        }

        try {
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(productDetails.subscriptionOfferDetails?.get(0)?.offerToken!!)
                            .build()
                    )
                ).build()

            val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Timber.e("Failed to launch billing flow: ${billingResult.debugMessage}")
                billingCallback.onPurchaseSuccess(false)
            }
        } catch (e: Exception) {
            Timber.e("Exception launching billing flow: ${e.message}")
            billingCallback.onPurchaseSuccess(false)
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}