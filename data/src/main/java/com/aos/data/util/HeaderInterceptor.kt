package com.aos.data.util

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

class HeaderInterceptor @Inject constructor(
    private val prefs: SharedPreferenceUtil
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        // Auth 헤더 처리
        if (originalRequest.headers["Auth"] == "false") {
            builder.removeHeader("Auth")
        } else {
            var token = ""
            runBlocking {
                token = "Bearer " + prefs.getString("accessToken", "")
            }
            Timber.e("token $token")
            builder.addHeader("Authorization", token)
        }

        // 구독 API 요청인지 확인하여 ostype 추가
        val url = originalRequest.url.toString()
        if (url.contains("/subscribe")) {  // "subscribe" 경로가 포함된 경우만 처리
            builder.addHeader("device", "android")
        }

        val newRequest = builder.build()
        return chain.proceed(newRequest)
    }
}
