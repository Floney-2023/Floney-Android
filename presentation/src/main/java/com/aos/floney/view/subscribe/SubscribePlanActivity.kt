package com.aos.floney.view.subscribe

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
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
        setUpClickableServiceText()
        setUpClickableRestoreText()
        viewModel.initBillingManager(this@SubscribePlanActivity)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cleanupBillingManager()
    }

    private fun setUpUi() {
        binding.setVariable(BR.eventHolder, this)
    }

    private fun setUpClickableServiceText() {
        val textView = binding.tvSubscribePlanInfromService
        val rawText = getString(R.string.subscribe_plan_infrom_service)
        val spanned = HtmlCompat.fromHtml(rawText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val spannable = SpannableString(spanned)

        val clickableText = "서비스 이용 약관"
        val start = spannable.indexOf(clickableText)
        val end = start + clickableText.length

        if (start >= 0) {
            val clickableSpan = object : ClickableSpan() {
                 override fun onClick(widget: View) {
                    viewModel.onClickService()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.color = ContextCompat.getColor(this@SubscribePlanActivity, R.color.grayscale2) // 색상은 원하는 대로
                }
            }

            spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            textView.text = spannable
            textView.movementMethod = LinkMovementMethod.getInstance()
            textView.highlightColor = Color.TRANSPARENT
        }
    }

    private fun setUpClickableRestoreText() {
        val textView = binding.tvSubscribePlanInfromNotice
        val rawText = getString(R.string.subscribe_plan_infrom_notice)
        val spanned = HtmlCompat.fromHtml(rawText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val spannable = SpannableString(spanned)

        val clickableText = "구매내역 복원하기"
        val start = spannable.indexOf(clickableText)
        val end = start + clickableText.length

        if (start >= 0) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    viewModel.onClickPlanRestore()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.color = ContextCompat.getColor(this@SubscribePlanActivity, R.color.grayscale2)
                }
            }

            spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            textView.text = spannable
            textView.movementMethod = LinkMovementMethod.getInstance()
            textView.highlightColor = Color.TRANSPARENT
        }
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
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://m.cafe.naver.com/floney/2"))
                    startActivity(intent)
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
                } else {
                    // Show error message
                    val errorToastDialog =
                        ErrorToastDialog(applicationContext, "결제가 실패되었습니다. 나중에 다시 시도해주세요.")
                    errorToastDialog.show()
                    Handler(Looper.myLooper()!!).postDelayed({
                        errorToastDialog.dismiss()
                    }, 2000)
                }
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