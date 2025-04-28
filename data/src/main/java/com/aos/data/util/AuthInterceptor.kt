package com.aos.data.util

import com.aos.data.BuildConfig
import com.aos.data.entity.response.token.PostUserReissueEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val prefs: SharedPreferenceUtil
) : okhttp3.Authenticator {

    // 세션 만료 이벤트를 전달하기 위한 SharedFlow
    private val _sessionExpiredEvent = MutableSharedFlow<Boolean>()
    val sessionExpiredEvent: SharedFlow<Boolean> = _sessionExpiredEvent

    override fun authenticate(route: Route?, response: Response): Request? {
        val originRequest = response.request

        if (originRequest.header("Authorization").isNullOrEmpty()) {
            return null
        }

        val refreshToken = prefs.getString("refreshToken", "")
        if (refreshToken.isBlank()) {
            Timber.e("Refresh token is empty or null")
            notifySessionExpired()
            return null
        }

        // CoroutineScope 내에서 실행하여 비동기적으로 토큰 재발급
        val refreshRequest = Request.Builder()
            .url("${BuildConfig.BASE_URL}users/reissue")
            .post(createTokenReissueRequestBody())
            .build()

        try {
            val refreshedToken = executeRefreshTokenRequest(refreshRequest)
            return refreshedToken?.let {
                updateTokenInPrefs(it.accessToken, it.refreshToken)
                originRequest.newBuilder().header("Authorization", "Bearer ${it.accessToken}").build()
            } ?: run {
                Timber.e("Failed to refresh token, token response is null")
                notifySessionExpired()
                null
            }
        } catch (e: IOException) {
            Timber.e(e, "Failed to refresh token due to IO exception")
            notifySessionExpired()
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during token refresh")
            notifySessionExpired()
        }

        // 새로운 요청을 기다리지 않고 null 반환
        return null
    }

    private fun notifySessionExpired() {
        CoroutineScope(Dispatchers.IO).launch {
            _sessionExpiredEvent.emit(true)
        }
    }

    private fun executeRefreshTokenRequest(refreshRequest: Request): PostUserReissueEntity? {
        val response = OkHttpClient().newCall(refreshRequest).execute()
        return response.use {
            if (response.isSuccessful) {
                val json = Json { ignoreUnknownKeys = true }
                val responseBody = response.body?.string()
                responseBody?.let {
                    try {
                        json.decodeFromString<PostUserReissueEntity>(it)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to parse token response")
                        null
                    }
                }
            } else {
                Timber.e("Token refresh failed with code: ${response.code}")
                clearTokens()
                null
            }
        }
    }

    private fun updateTokenInPrefs(accessToken: String, refreshToken: String) {
        if (accessToken.isBlank() || refreshToken.isBlank()) {
            Timber.e("Received empty tokens from server")
            return
        }

        prefs.setString("accessToken", accessToken)
        prefs.setString("refreshToken", refreshToken)
    }

    private fun clearTokens() {
        prefs.setString("accessToken", "")
        prefs.setString("refreshToken", "")
    }

    private fun createTokenReissueRequestBody(): RequestBody {
        val accessToken = prefs.getString("accessToken", "")
        val refreshToken = prefs.getString("refreshToken", "")

        Timber.d("Creating token reissue request with access token length: ${accessToken.length}, refresh token length: ${refreshToken.length}")

        val requestBodyString = """
        {
            "accessToken": "$accessToken",
            "refreshToken": "$refreshToken"
        }
    """.trimIndent()

        return requestBodyString.toRequestBody("application/json".toMediaTypeOrNull())
    }
}