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
import androidx.annotation.RequiresApi
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
import com.aos.floney.ext.setStatusBarTransparent
import com.aos.floney.util.NetworkUtils
import com.aos.floney.util.RemoteConfigWrapper
import com.aos.floney.view.book.entrance.BookEntranceActivity
import com.aos.floney.view.common.BaseAlertDialog
import com.aos.floney.view.common.WarningPopupDialog
import com.aos.floney.view.home.HomeActivity
import com.aos.floney.view.settleup.SettleUpActivity
import com.aos.floney.view.signup.SignUpCompleteActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity :
    BaseActivity<ActivitySplashBinding, SplashViewModel>(R.layout.activity_splash) {

    @Inject
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    @Inject
    lateinit var remoteConfigWrapper: RemoteConfigWrapper

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupSplashAnimation()
        setStatusBarTransparent()
        setPreferenceUtil()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkPauseUpdate(
        maintenanceStart: LocalDateTime,
        maintenanceEnd: LocalDateTime
    ) {
        val formattedStart = maintenanceStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val formattedEnd = maintenanceEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

        val message = getString(
            R.string.maintenance_message,
            formattedStart,
            formattedEnd
        )

        BaseAlertDialog(
            title = getString(R.string.maintenance_title),
            info = message,
            false
        ) {
            finishAffinity()
        }.show(supportFragmentManager, "PauseUpdateDialog")
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
            } else if(sharedPreferenceUtil.getString("accessToken", "") != "" && !viewModel.getSessionExpiredFlag()) {
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
        return try {
            packageManager.getPackageInfo(packageName, 0)?.versionName ?: "0.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "0.0.0"
        }
    }
    fun isUpdateRequired(latestVersion: String?, currentVersion: String): Boolean {
        if (latestVersion == null) return false

        val latestVersionParts = latestVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val currentVersionParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }

        // 두 버전 중 더 긴 부분 길이만큼 0으로 패딩하여 비교
        val maxLength = maxOf(latestVersionParts.size, currentVersionParts.size)
        val paddedLatest = latestVersionParts + List(maxLength - latestVersionParts.size) { 0 }
        val paddedCurrent = currentVersionParts + List(maxLength - currentVersionParts.size) { 0 }

        for (i in 0 until maxLength) {
            if (paddedLatest[i] > paddedCurrent[i]) return true
            if (paddedLatest[i] < paddedCurrent[i]) return false
        }
        return false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupSplashAnimation() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.splash_animation)
        binding.ivAppLogo.startAnimation(animation)

        Handler(Looper.myLooper()!!).postDelayed({
            checkServerStatusAndUpdate()
        }, 2000)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkServerStatusAndUpdate() {
        if (!NetworkUtils.isNetworkConnected(this)) {

            val exitDialogFragment = WarningPopupDialog(
                title = "네트워크 연결 오류",
                info = "인터넷 연결을 확인한 후 다시 시도해주세요.\n앱을 종료합니다.",
                leftButton = "",
                rightButton = getString(R.string.already_pick_button),
                check = true
            ) { finishAffinity() }

            exitDialogFragment.show(supportFragmentManager, "initDialog")
            return
        }

        // RemoteConfigWrapper에서 점검 시작 및 종료 시간 가져오기
        val (maintenanceStart, maintenanceEnd) = remoteConfigWrapper.fetchMaintenanceTimes()
        val currentDateTime = LocalDateTime.now()

        if (currentDateTime.isAfter(maintenanceStart) && currentDateTime.isBefore(maintenanceEnd)) {
            checkPauseUpdate(maintenanceStart, maintenanceEnd) // 일시 중지 팝업
        }
        else {
            checkForMandatoryUpdate() // 버전 업데이트 판단 여부
        }
    }


    private fun checkForMandatoryUpdate() { // 강제 업데이트 팝업
        val minSupportedVersion = remoteConfigWrapper.fetchAndActivateConfig()
        val currentVersion = getCurrentAppVersion()

        Timber.e("minSupportVersion : ${minSupportedVersion} currentVersion : ${currentVersion}")
        if (isUpdateRequired(minSupportedVersion, currentVersion)) {
            showUpdateDialog() // 강제 업데이트 팝업
        } else {
            navigateToScreen() // 업데이트가 필요하지 않으면 다음 화면으로 이동
        }
    }

    private fun showUpdateDialog() {
        BaseAlertDialog(title = getString(R.string.dialog_update_title), info = getString(R.string.dialog_update_info), false) {
            if(it) {
                redirectToPlayStore()
            }
        }.show(supportFragmentManager, "showUpdateDialog")
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
                        intent.putExtra("bookCode", it.getQueryParameter("bookCode"))

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

    private fun setPreferenceUtil(){
        CurrencyUtil.currency = sharedPreferenceUtil.getString("symbol", "원")
        sharedPreferenceUtil.setString("subscribeCheckTenMinutes", "")
    }
}