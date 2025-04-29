package com.aos.floney.di

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ProcessLifecycleOwner
import com.aos.data.util.AuthInterceptor
import com.aos.floney.BuildConfig
import com.aos.floney.util.SessionManager
import com.aos.floney.view.login.LoginActivity
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class AppApplication : Application(), Application.ActivityLifecycleCallbacks {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var authInterceptor: AuthInterceptor

    private val appScope = CoroutineScope(Dispatchers.Main)
    private var currentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        KakaoSdk.init(this, BuildConfig.kakao_native_app_key)

        // 액티비티 생명주기 콜백 등록
        registerActivityLifecycleCallbacks(this)

        // 세션 만료 이벤트 관찰
        observeSessionExpired()
    }

    private fun observeSessionExpired() {
        appScope.launch {
            authInterceptor.sessionExpiredEvent.collectLatest { isExpired ->
                if (isExpired) {
                    Timber.d("세션이 만료되었습니다. 로그인 화면으로 이동합니다.")
                    authInterceptor.clearTokens()
                    currentActivity?.let { activity ->
                        if (activity !is LoginActivity) {
                            // 로그인 화면으로 이동
                            sessionManager.handleSessionExpired(activity)
                        }
                    }
                }
            }
        }
    }

    // ActivityLifecycleCallbacks 구현
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity === activity) {
            currentActivity = null
        }
    }
}