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
import com.aos.floney.base.BaseViewModel
import com.aos.floney.databinding.ActivityBookEntranceBinding
import com.aos.floney.databinding.ActivitySubscribePlanBinding
import com.aos.floney.ext.applyCloseTransition
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.BaseAlertDialog
import com.aos.floney.view.common.ErrorToastDialog
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpUi()
        setUpViewModelObserver()
        viewModel.initBillingManager(this@SubscribePlanActivity)
    }

    private fun setUpUi() {
        binding.setVariable(BR.eventHolder, this)
    }

    private fun setUpViewModelObserver() {
        repeatOnStarted {
            // 구독하기
            viewModel.subscribeChannel.collect {
                if(it) {
                    viewModel.startSubscribeConnection()
                }
            }
        }
        repeatOnStarted {
            // 구매 정보 복원하기
            viewModel.subscribeRestore.collect {
                if(it) {
                    // 플레이스토어 정기 결제 내역으로 이동
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
        repeatOnStarted {
            // 구독 성공 bottomSheet 표시 및 마이페이지 화면으로 이동
            viewModel.subscribeSuccess.collect {
                if(it) {
                    showsubscribeComplete()
                } else
                    viewModel.baseEvent(BaseViewModel.Event.ShowToast("결제가 실패되었습니다. 플로니팀으로 연락 부탁드립니다."))
            }
        }
    }

    fun showsubscribeComplete(){
        // 구독 시작 팝업
        val exitDialogFragment = WarningPopupDialog(
            getString(R.string.subscribe_popup_title),
            getString(R.string.subscribe_popup_info),
            "",getString(R.string.already_pick_button),
            true
        ) { checked ->
            if (!checked){
                val intent = Intent(this@SubscribePlanActivity, MyPageActivity::class.java)
                startActivity(intent)
                applyCloseTransition()
                finishAffinity()
            }
        }
        exitDialogFragment.show(supportFragmentManager, "initDialog")
    }
}