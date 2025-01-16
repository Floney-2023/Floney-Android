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
import com.aos.floney.databinding.ActivitySubscribePlanBinding
import com.aos.floney.databinding.ActivityUnsubscribeBinding
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
class UnsubscribeActivity : BaseActivity<ActivityUnsubscribeBinding, UnsubscribeViewModel>(R.layout.activity_unsubscribe) {

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
            // 구독 유지하기
            viewModel.resubscribe.collect {
                if(it) {
                    finish()
                }
            }
        }
        repeatOnStarted {
            // 구독 해지하기
            viewModel.unsubscribePage.collect {
                if(it) {
                    // 구독 해지 여부 확인 팝업
                    BaseAlertDialog(title = getString(R.string.unsubscribe_notice_pop_title), info = getString(R.string.unsubscribe_notice_pop_info), true) {
                        if(it) {

                            val packageName = this@UnsubscribeActivity.packageName // 현재 앱의 패키지 이름
                            val uri = Uri.parse("https://play.google.com/store/account/subscriptions?package=$packageName")

                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.android.vending") // Play Store 앱으로만 열리도록 설정
                            }

                            // Play Store 앱이 설치되어 있는지 확인
                            if (intent.resolveActivity(this@UnsubscribeActivity.packageManager) != null) {
                                this@UnsubscribeActivity.startActivity(intent)
                            } else {
                                // Play Store 앱이 없는 경우 처리
                                Timber.e("Playstore 필요")
                            }

                        }
                    }.show(supportFragmentManager, "baseAlertDialog")
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

        repeatOnStarted {
            viewModel.subscribePage.collect {
                if(!it) { // 구독 해지 완료(구독 false인 상태)면, 팝업
                    showUnsubscribeComplete()
                } else{ // 해지 안했을 경우

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // 구독 상태를 다시 확인하는 코드 필요
        viewModel.getSubscribeStatus()
    }

    fun showUnsubscribeComplete(){
        // 구독 해지 완료 팝업
        val exitDialogFragment = WarningPopupDialog(
            getString(R.string.unsubscribe_popup_title),
            getString(R.string.unsubscribe_popup_info),
            getString(R.string.already_pick_button),getString(R.string.already_pick_button),
            true
        ) { checked ->
            if (!checked){
                val intent = Intent(this@UnsubscribeActivity, MyPageActivity::class.java)
                startActivity(intent)
                if (Build.VERSION.SDK_INT >= 34) {
                    overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                } else {
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
                finishAffinity()
            }
        }
        exitDialogFragment.show(supportFragmentManager, "initDialog")
    }
}