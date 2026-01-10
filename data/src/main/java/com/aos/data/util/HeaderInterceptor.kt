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
            Timber.d("token $token")
            builder.addHeader("Authorization", token)
        }

        val newRequest = builder.build()
        return chain.proceed(newRequest)
    }
}
