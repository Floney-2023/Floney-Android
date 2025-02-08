package com.aos.floney.view.subscribe

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.aos.floney.view.common.ErrorToastDialog
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
    override fun onResume() {
        super.onResume()
        viewModel.getSubscribeData()
    }
    private fun setUpUi() {
        binding.setVariable(BR.vm, viewModel)
        binding.setVariable(BR.eventHolder, this)
    }
    private fun setUpViewModelObserver() {
        repeatOnStarted {
            // 다시 구독하기
            viewModel.resubscribe.collect {
                if(it) {
                    val packageName = packageName // 현재 앱의 패키지 이름
                    val uri = Uri.parse("https://play.google.com/store/account/subscriptions?package=$packageName")

                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.android.vending") // Play Store 앱으로만 열리도록 설정
                    }

                    // Play Store 앱이 설치되어 있는지 확인
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    } else {
                        // Play Store 앱이 없는 경우 팝업
                        val errorToastDialog = ErrorToastDialog(applicationContext, "플레이 스토어가 설치 되어 있지 않습니다.")
                        errorToastDialog.show()

                        Handler(Looper.myLooper()!!).postDelayed({
                            errorToastDialog.dismiss()
                        }, 2000)
                    }
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