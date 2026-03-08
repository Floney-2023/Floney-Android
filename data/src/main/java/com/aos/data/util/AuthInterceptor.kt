package com.aos.data.util

import com.aos.data.BuildConfig
import com.aos.data.entity.response.token.PostUserReissueEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val prefs: SharedPreferenceUtil
) : Authenticator {

    private val _sessionExpiredEvent = MutableSharedFlow<Boolean>()
    val sessionExpiredEvent: SharedFlow<Boolean> = _sessionExpiredEvent

    private val refreshLock = Any()
    @Volatile private var sessionExpired = false

    override fun authenticate(route: Route?, response: Response): Request? {
        Timber.e(
            "[AuthInterceptor] authenticate() called: code=%d, url=%s",
            response.code,
            response.request.url
        )
        if (responseCount(response) >= 2) {
            Timber.e("[AuthInterceptor] Too many token refresh attempts")
            return null
        }

        if (sessionExpired) {
            Timber.e("[AuthInterceptor] Session already expired, skip reissue")
            return null
        }

        val originRequest = response.request
        if (originRequest.header("Authorization").isNullOrEmpty()) {
            Timber.d("[AuthInterceptor] Authorization header missing, skip reissue")
            return null
        }

        val refreshToken = prefs.getString("refreshToken", "")
        if (refreshToken.isBlank()) {
            Timber.e("[AuthInterceptor] Refresh token is empty")
            triggerSessionExpiredOnce()
            return null
        }

        synchronized(refreshLock) {
            if (sessionExpired) {
                Timber.e("[AuthInterceptor] Session expired while waiting lock, skip reissue")
                return null
            }

            val currentAuthHeader = "Bearer ${prefs.getString("accessToken", "")}"
            val requestAuthHeader = originRequest.header("Authorization")
            if (!requestAuthHeader.isNullOrBlank() &&
                requestAuthHeader != currentAuthHeader &&
                currentAuthHeader != "Bearer "
            ) {
                Timber.e("[AuthInterceptor] Token already refreshed by another request, retrying with latest token")
                return originRequest.newBuilder()
                    .header("Authorization", currentAuthHeader)
                    .build()
            }

            try {
                Timber.e("[AuthInterceptor] Start reissue request for url=%s", originRequest.url)
                val refreshRequest = Request.Builder()
                    .url("${BuildConfig.BASE_URL}users/reissue")
                    .post(createTokenReissueRequestBody())
                    .build()

                Timber.e("[AuthInterceptor] Sending refresh token request")
                val refreshedToken = executeRefreshTokenRequest(refreshRequest)

                if (refreshedToken != null) {
                    Timber.e("[AuthInterceptor] Reissue success, updating tokens")
                    updateTokenInPrefs(refreshedToken.accessToken, refreshedToken.refreshToken)

                    Timber.e("[AuthInterceptor] Retrying original request with new access token")
                    return originRequest.newBuilder()
                        .header("Authorization", "Bearer ${refreshedToken.accessToken}")
                        .build()
                } else {
                    Timber.e("[AuthInterceptor] Reissue failed: null body")
                    triggerSessionExpiredOnce()
                    return null
                }
            } catch (e: Exception) {
                Timber.e(e, "[AuthInterceptor] Error during token refresh")
                triggerSessionExpiredOnce()
                return null
            }
        }
    }

    private fun triggerSessionExpiredOnce() {
        if (!sessionExpired) {
            sessionExpired = true
            Timber.e("[AuthInterceptor] Trigger session expired event")
            CoroutineScope(Dispatchers.IO).launch {
                _sessionExpiredEvent.emit(true)
            }
        }
    }

    private fun updateTokenInPrefs(accessToken: String, refreshToken: String) {
        if (accessToken.isNotBlank() && refreshToken.isNotBlank()) {
            prefs.setString("accessToken", accessToken)
            prefs.setString("refreshToken", refreshToken)
        } else {
            Timber.e("Empty tokens received")
        }
    }

    private fun createTokenReissueRequestBody(): RequestBody {
        val body = """
            {
                "accessToken": "${prefs.getString("accessToken", "")}",
                "refreshToken": "${prefs.getString("refreshToken", "")}"
            }
        """.trimIndent()

        Timber.d("Refresh Body: $body")
        return body.toRequestBody("application/json".toMediaTypeOrNull())
    }

    private fun executeRefreshTokenRequest(request: Request): PostUserReissueEntity? {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .authenticator(Authenticator.NONE)
            .build()

        return client.newCall(request).execute().use { response ->
            Timber.e("[AuthInterceptor] Reissue response code=%d", response.code)
            if (response.isSuccessful) {
                response.body?.string()?.let {
                    try {
                        Timber.e("[AuthInterceptor] Reissue response parsing success")
                        Json { ignoreUnknownKeys = true }.decodeFromString(it)
                    } catch (e: Exception) {
                        Timber.e(e, "[AuthInterceptor] Token parsing failed")
                        null
                    }
                }
            } else {
                Timber.e("[AuthInterceptor] Reissue failed with code=%d", response.code)
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    fun clearTokens() {
        prefs.setString("accessToken", "")
        prefs.setString("refreshToken", "")
        sessionExpired = false
    }

    fun resetSessionExpiredFlag() {
        sessionExpired = false
    }

    fun getSessionExpiredFlag(): Boolean = sessionExpired
}
