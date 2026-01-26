package com.aos.floney.view.book.entrance

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
import com.aos.floney.ext.applyOpenTransition
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
class BookEntranceActivity : BaseActivity<ActivityBookEntranceBinding, BookEntranceViewModel>(R.layout.activity_book_entrance) {

    @Inject
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpUi()
        setUpViewModelObserver()
        setUpEntranceDeepLink()
    }

    private fun setUpUi() {
        binding.setVariable(BR.eventHolder, this)
        setInviteCode(intent.getStringExtra("settlementId")?:"")
    }
    private fun setUpViewModelObserver() {
        repeatOnStarted {
            // 나중에 하기 -> Bottomsheet 닫기
            viewModel.inviteCodeExit.collect {
                Timber.e("nextPage $it")
                if(it) { // 가계부 존재(bookKey 유효 O, 로그인 유효 O)
                    val intent = Intent(this@BookEntranceActivity, HomeActivity::class.java)
                    startActivity(intent)
                    if (Build.VERSION.SDK_INT >= 34) {
                        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                    } else {
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                    finishAffinity()
                }else{
                    if(sharedPreferenceUtil.getString("accessToken", "") == "") { // accessToken 유효 X
                        val intent = Intent(this@BookEntranceActivity, LoginActivity::class.java)
                        startActivity(intent)
                        if (Build.VERSION.SDK_INT >= 34) {
                            overrideActivityTransition(
                                Activity.OVERRIDE_TRANSITION_OPEN,
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                            )
                        } else {
                            overridePendingTransition(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                            )
                        }
                        finishAffinity()
                    }
                    else{
                        val intent = Intent(this@BookEntranceActivity, SignUpCompleteActivity::class.java)
                        startActivity(intent)
                        if (Build.VERSION.SDK_INT >= 34) {
                            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                        } else {
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }

                    }
                }
            }
        }
        repeatOnStarted {
            // 초대 코드 클립보드 복사
            viewModel.inviteCodeCopy.collect {
                Timber.e("nextPage $it")
                if(it) {
                    val code = viewModel.inviteCode.value ?: ""
                    val clipboard: ClipboardManager =
                        this@BookEntranceActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("label", code)
                    clipboard.setPrimaryClip(clip)
                    setDialog()
                }
            }
        }
        repeatOnStarted {
            viewModel.newBookCreatePage.collect{
                if (it){
                    startActivity(Intent(this@BookEntranceActivity, HomeActivity::class.java))
                    if (Build.VERSION.SDK_INT >= 34) {
                        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                    } else {
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                }
            }
        }
        repeatOnStarted {
            viewModel.codeInputPage.collect{
                startActivity(Intent(this@BookEntranceActivity, MyPageBookCodeInputActivity::class.java))
                if (Build.VERSION.SDK_INT >= 34) {
                    overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                } else {
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        }
    }
    private fun setDialog()
    {
        BaseAlertDialog(
            title = getString(R.string.invite_code_copy_title),
            info = getString(R.string.invite_code_copy_message),
            false
        ) {
            if(it) {

            }
        }.show(supportFragmentManager, "baseAlertDialog")
    }
    private fun setUpEntranceDeepLink() {
        AppsFlyerLib.getInstance().init(BuildConfig.appsflyer_dev_key, null, this)
        AppsFlyerLib.getInstance().start(this)

        AppsFlyerLib.getInstance().subscribeForDeepLink(object : DeepLinkListener {
            override fun onDeepLinking(deepLinkResult: DeepLinkResult) {
                when (deepLinkResult.status) {
                    DeepLinkResult.Status.FOUND -> {
                        val deepLinkObj = deepLinkResult.deepLink
                        val inviteCode = deepLinkObj.values["inviteCode"]
                        if(sharedPreferenceUtil.getString("accessToken", "") == "") { // 로그인 상태 X

                            val intent = Intent(this@BookEntranceActivity, LoginActivity::class.java)

                            startActivity(intent)
                            if (Build.VERSION.SDK_INT >= 34) {
                                overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                            } else {
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            }
                            finishAffinity()
                        }
                        else{
                            setInviteCode(inviteCode)
                        }
                    }
                    DeepLinkResult.Status.NOT_FOUND -> {
                        Timber.d("Deep link not found")
                        handleFallbackUriScheme()
                    }
                    else -> {
                        val dlError = deepLinkResult.error
                        Timber.d("There was an error getting Deep Link data: $dlError")
                        handleFallbackUriScheme()
                    }
                }
                val deepLinkObj: DeepLink = deepLinkResult.deepLink
                try {
                    Timber.d("The DeepLink data is: $deepLinkObj")
                } catch (e: Exception) {
                    Timber.d("DeepLink data came back null")
                    return
                }

                if (deepLinkObj.isDeferred == true) {
                    Timber.d("This is a deferred deep link")
                } else {
                    Timber.d("This is a direct deep link")
                }

                try {
                    val fruitName = deepLinkObj.deepLinkValue
                    Timber.d("The DeepLink will route to: $fruitName")
                } catch (e: Exception) {
                    Timber.d("There's been an error: $e")
                    return
                }
            }
        })
    }

    private fun handleFallbackUriScheme() {
        val fallbackUri = "floney://" // 실제 URI 스킴으로 변경
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUri))
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.d("Fallback URI scheme not found: $e")
        }
    }

    fun setInviteCode(inviteCode: Any) {
        if (inviteCode != "") {
            Timber.d("DeepLink", "Invite Code: $inviteCode")
            viewModel.setinviteCode(inviteCode.toString())
        } else {
            Timber.d("DeepLink", "Invite Code not found")
        }
    }
}