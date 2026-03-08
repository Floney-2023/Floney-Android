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
            val token = "Bearer " + prefs.getString("accessToken", "")
            Timber.d("token $token")
            builder.addHeader("Authorization", token)
        }

        val newRequest = builder.build()
        Timber.e(
            "[HeaderInterceptor] request url=%s, hasAuth=%s, authorization=%s",
            newRequest.url,
            newRequest.header("Auth") != null,
            newRequest.header("Authorization")?.take(24)
        )

        val response = chain.proceed(newRequest)
        if (response.code == 401) {
            Timber.e(
                "[HeaderInterceptor] 401 received url=%s, authorization=%s",
                newRequest.url,
                newRequest.header("Authorization")?.take(24)
            )
        }
        return response
    }
}
