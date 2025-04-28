// 새 파일 생성
package com.aos.floney.util

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.aos.data.util.AuthInterceptor
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.view.login.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val authInterceptor: AuthInterceptor,
    private val prefs: SharedPreferenceUtil
) : DefaultLifecycleObserver {

    private val sessionScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        if (owner is ComponentActivity) {
            observeSessionExpired(owner)
        }
    }

    private fun observeSessionExpired(activity: ComponentActivity) {
        sessionScope.launch {
            authInterceptor.sessionExpiredEvent.collectLatest { isExpired ->
                if (isExpired) {
                    Timber.d("세션이 만료되어 자동 재발급 시도 실패. 로그인 화면으로 이동합니다.")
                    handleSessionExpired(activity)
                }
            }
        }
    }

    fun handleSessionExpired(activity: Activity) {
        // 현재 활동 중인 액티비티가 이미 LoginActivity라면 무시
        if (activity is LoginActivity) {
            return
        }

        // 로그인 정보 확인 (소셜 로그인으로 쉽게 재인증 가능한지)
        val hasKakaoAccount = prefs.getBoolean("has_kakao_account", false)
        val hasGoogleAccount = prefs.getBoolean("has_google_account", false)
        val hasNaverAccount = prefs.getBoolean("has_naver_account", false)
        val hasAppleAccount = prefs.getBoolean("has_apple_account", false)

        // UI 스레드에서 실행
        activity.runOnUiThread {
            Toast.makeText(activity, "로그인이 만료되었습니다. 다시 로그인해주세요.", Toast.LENGTH_LONG).show()

            // 모든 액티비티 스택을 비우고 로그인 화면으로 이동
            val intent = Intent(activity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                // 소셜 로그인 정보가 있으면 간편 로그인을 위한 플래그 추가
                putExtra("expired_session", true)
                putExtra("has_kakao_account", hasKakaoAccount)
                putExtra("has_google_account", hasGoogleAccount)
                putExtra("has_naver_account", hasNaverAccount)
                putExtra("has_apple_account", hasAppleAccount)
            }
            activity.startActivity(intent)
            activity.finish()
        }
    }
}