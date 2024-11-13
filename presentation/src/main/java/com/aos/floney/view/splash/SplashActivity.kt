package com.aos.floney.view.splash

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.databinding.ActivitySplashBinding
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.view.login.LoginActivity
import com.aos.floney.view.onboard.OnBoardActivity
import com.aos.data.util.CurrencyUtil
import com.aos.floney.BuildConfig
import com.aos.floney.view.book.entrance.BookEntranceActivity
import com.aos.floney.view.common.WarningPopupDialog
import com.aos.floney.view.home.HomeActivity
import com.aos.floney.view.settleup.SettleUpActivity
import com.aos.floney.view.signup.SignUpCompleteActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.security.MessageDigest
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity :
    BaseActivity<ActivitySplashBinding, SplashViewModel>(R.layout.activity_splash) {

    @Inject
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getAppKeyHash()
        setupSplashAnimation()
        setStatusBarTransparent()
        CurrencyUtil.currency = sharedPreferenceUtil.getString("symbol", "원")
    }
    private fun checkForUpdate() {
        val currentVersion = getCurrentAppVersion() // 현재 앱의 버전 가져오기
        val latestVersion = "1.1.12" // 서버에서 최신 버전 가져오기(임의)
        val updateRequired = isUpdateRequired(latestVersion, currentVersion)

        if (updateRequired) {
            // 강제 업데이트 안내 팝업

            val exitDialogFragment = WarningPopupDialog(
                "서버 점검으로 인한 이용 일시 중단",
                "서버 변경 작업으로 인해 14일 22:00 ~ 15일 09:00 기간까지 앱을 사용할 수 없습니다.\n" +
                        "불편을 드려 죄송합니다.",
                "확인",
                "확인",
                true
            ) { checked ->
                if (checked){
                    finishAffinity()
                    android.os.Process.killProcess(android.os.Process.myPid())
                    // redirectToPlayStore() // Google Play 스토어로 리다이렉트
                }
            }

            exitDialogFragment.show(supportFragmentManager, "initDialog")
        } else {
            // 업데이트가 필요하지 않으면 기존 로직대로
            navigateToScreen()
        }
    }

    private fun redirectToPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
        }
        startActivity(intent)
        finish()
    }

    // 2초 후 처음 실행할 경우 온보드 아니면 로그인으로 이동
    private fun navigateToScreen() {
        if (sharedPreferenceUtil.getBoolean(getString(R.string.is_first), true)) {
            val intent = Intent(this@SplashActivity, OnBoardActivity::class.java)
            startActivity(intent)
            if (Build.VERSION.SDK_INT >= 34) {
                overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
            } else {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        } else {
            // 자동 로그인 기능 구현
            if(sharedPreferenceUtil.getString("accessToken", "") != "" && sharedPreferenceUtil.getString("bookKey", "") == "") {
                val intent = Intent(this@SplashActivity, SignUpCompleteActivity::class.java)
                startActivity(intent)
                if (Build.VERSION.SDK_INT >= 34) {
                    overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                } else {
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            } else if(sharedPreferenceUtil.getString("accessToken", "") != "") {
                handleIntent(intent)
            }  else {
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(intent)
                if (Build.VERSION.SDK_INT >= 34) {
                    overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                } else {
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        }
        finish()
    }

    private fun getCurrentAppVersion(): String {
        return packageManager.getPackageInfo(packageName, 0).versionName
    }
    fun isUpdateRequired(latestVersion: String?, currentVersion: String): Boolean {
        if (latestVersion == null) return false

        val latestVersionParts = latestVersion.split(".")
        val currentVersionParts = currentVersion.split(".")

        val maxLength = maxOf(latestVersionParts.size, currentVersionParts.size)
        for (i in 0 until maxLength) {
            val latestPart = latestVersionParts.getOrNull(i)?.toIntOrNull() ?: 0
            val currentPart = currentVersionParts.getOrNull(i)?.toIntOrNull() ?: 0
            if (latestPart > currentPart) return true
            if (latestPart < currentPart) return false
        }
        return false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    // logo animation
    private fun setupSplashAnimation() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.splash_animation)
        binding.ivAppLogo.startAnimation(animation)

        Handler(Looper.myLooper()!!).postDelayed({
            checkForUpdate() // 버전 업데이트 확인
        }, 2000)
    }
    private fun handleIntent(intent: Intent) {
        val data: Uri? = intent.data
        if (data != null) {
            navigateToAppropriateActivity(data)
        } else {
            // 딥 링크가 없을 경우 홈 화면으로 이동
            val intent = Intent(this@SplashActivity, HomeActivity::class.java)
            startActivity(intent)
            if (Build.VERSION.SDK_INT >= 34) {
                overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
            } else {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }

    private fun navigateToAppropriateActivity(data: Uri) {
        data?.let {
            val campaign = it.getQueryParameter("campaign")
            when (campaign) {
                "floney_share" -> {
                    if(sharedPreferenceUtil.getString("accessToken", "") == "") { // accessToken 유효 X
                        val inviteCode = it.getQueryParameter("inviteCode")
                        val intent = Intent(this@SplashActivity, LoginActivity::class.java)

                        // 데이터를 Intent에 추가
                        intent.putExtra("settlementId", it.getQueryParameter("inviteCode"))

                        startActivity(intent)
                        if (Build.VERSION.SDK_INT >= 34) {
                            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                        } else {
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                    }
                    else {
                        val inviteCode = it.getQueryParameter("inviteCode")
                        val intent = Intent(this@SplashActivity, BookEntranceActivity::class.java)

                        // 데이터를 Intent에 추가
                        intent.putExtra("settlementId", it.getQueryParameter("inviteCode"))

                        startActivity(intent)
                        if (Build.VERSION.SDK_INT >= 34) {
                            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                        } else {
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                    }

                }
                "floney_settlement_share" -> {
                    if(sharedPreferenceUtil.getString("accessToken", "") == "") { // accessToken 유효 X
                        val inviteCode = it.getQueryParameter("inviteCode")
                        val intent = Intent(this@SplashActivity, LoginActivity::class.java)

                        // 데이터를 Intent에 추가
                        intent.putExtra("settlementId", it.getQueryParameter("inviteCode"))

                        startActivity(intent)
                        if (Build.VERSION.SDK_INT >= 34) {
                            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                        } else {
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                    }
                    else {
                        val intent = Intent(this@SplashActivity, SettleUpActivity::class.java)

                        // 데이터를 Intent에 추가
                        intent.putExtra("settlementId", it.getQueryParameter("settlementId"))
                        intent.putExtra("bookKey", it.getQueryParameter("bookKey"))

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
    }
    private fun getAppKeyHash() {
        try {
            val info = packageManager.getPackageInfo(packageName, GET_SIGNATURES)
            for (signature in info.signatures) {
                var md: MessageDigest
                md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val something = String(Base64.encode(md.digest(), 0))
                Timber.e("Hashkey ${something}")
            }
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            Log.e("name not found", e.toString())
        }
    }
    private fun AppCompatActivity.setStatusBarTransparent() {

        window.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        }

        if (Build.VERSION.SDK_INT >= 30) {    // API 30 에 적용
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }
}