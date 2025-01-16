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
import com.aos.floney.databinding.ActivitySubscribeInformBinding
import com.aos.floney.databinding.ActivitySubscribePlanBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.BaseAlertDialog
import com.aos.floney.view.common.WarningPopupDialog
import com.aos.floney.view.home.HomeActivity
import com.aos.floney.view.login.LoginActivity
import com.aos.floney.view.mypage.MyPageActivity
import com.aos.floney.view.mypage.bookadd.codeinput.MyPageBookCodeInputActivity
import com.aos.floney.view.settleup.SettleUpActivity
import com.aos.floney.view.signup.SignUpCompleteActivity
import com.appsflyer.AppsFlyerLib
import com.appsflyer.deeplink.DeepLink
import com.appsflyer.deeplink.DeepLinkListener
import com.appsflyer.deeplink.DeepLinkResult
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SubscribeInformActivity : BaseActivity<ActivitySubscribeInformBinding, SubscribeInformViewModel>(R.layout.activity_subscribe_inform) {

    @Inject
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpUi()
        setUpViewModelObserver()
    }

    private fun setUpUi() {
        binding.setVariable(BR.eventHolder, this)
    }
    private fun setUpViewModelObserver() {
        repeatOnStarted {
            // 다시 구독하기
            viewModel.resubscribe.collect {
                if(it) {
                    viewModel.initBillingManager(this@SubscribeInformActivity)
                }
            }
        }
        repeatOnStarted {
            // 구독 해지하기 페이지로 이동
            viewModel.unsubscribePage.collect {
                if(it) {
                    val intent = Intent(this@SubscribeInformActivity, UnsubscribeActivity::class.java)
                    startActivity(intent)
                    if (Build.VERSION.SDK_INT >= 34) {
                        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                    } else {
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
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