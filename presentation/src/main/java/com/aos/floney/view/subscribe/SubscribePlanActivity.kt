package com.aos.floney.view.subscribe

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.databinding.library.baseAdapters.BR
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.BuildConfig
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.databinding.ActivityBookEntranceBinding
import com.aos.floney.databinding.ActivitySubscribePlanBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.BaseAlertDialog
import com.aos.floney.view.common.WarningPopupDialog
import com.aos.floney.view.home.HomeActivity
import com.aos.floney.view.login.LoginActivity
import com.aos.floney.view.mypage.MyPageActivity
import com.aos.floney.view.mypage.bookadd.codeinput.MyPageBookCodeInputActivity
import com.aos.floney.view.signup.SignUpCompleteActivity
import com.appsflyer.AppsFlyerLib
import com.appsflyer.deeplink.DeepLink
import com.appsflyer.deeplink.DeepLinkListener
import com.appsflyer.deeplink.DeepLinkResult
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SubscribePlanActivity : BaseActivity<ActivitySubscribePlanBinding, SubscribePlanViewModel>(R.layout.activity_subscribe_plan) {

    @Inject
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpUi()
        setUpViewModelObserver()
    }

    private fun setUpUi() {
        binding.setVariable(BR.eventHolder, this)
    }
    private fun setUpBillingManager()
    {
        viewModel.initBillingManager(this)
    }
    private fun setUpViewModelObserver() {
        repeatOnStarted {
            // 구독하기
            viewModel.subscribe.collect {
                if(it) {
                    viewModel.initBillingManager(this@SubscribePlanActivity)
                }
            }
        }
        repeatOnStarted {
            // 구매 정보 복원하기
            viewModel.subscribeRestore.collect {
                if(it) {
                    finish()
                }
            }
        }
        repeatOnStarted {
            // 서비스 이용 내역 화면
            viewModel.servicePage.collect {
                if(it) {
                    finish()
                }
            }
        }
        repeatOnStarted {
            // 이전 화면으로
            viewModel.back.collect {
                if(it) {
                    finish()
                }
            }
        }
    }
}