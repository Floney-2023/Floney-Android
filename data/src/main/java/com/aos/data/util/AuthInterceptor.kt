package com.aos.data.util

import com.aos.data.BuildConfig
import com.aos.data.entity.response.token.PostUserReissueEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private val refreshTokenMutex = Mutex()
    @Volatile private var sessionExpired = false
    private var lastRefreshFailed = false

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            Timber.e("Too many token refresh attempts")
            return null
        }

        if (sessionExpired) return null

        val originRequest = response.request
        if (originRequest.header("Authorization").isNullOrEmpty()) return null

        val refreshToken = prefs.getString("refreshToken", "")
        if (refreshToken.isBlank()) {
            Timber.e("Refresh token is empty")
            triggerSessionExpiredOnce()
            return null
        }

        return runBlocking(Dispatchers.IO) {
            refreshTokenMutex.withLock {
                if (sessionExpired) return@runBlocking null

                // 중복 호출 방지: 이전 refresh가 실패한 경우 바로 종료
                if (lastRefreshFailed) {
                    Timber.e("Previous refresh failed, skipping")
                    return@withLock null
                }

                try {
                    val refreshRequest = Request.Builder()
                        .url("${BuildConfig.BASE_URL}users/reissue")
                        .post(createTokenReissueRequestBody())
                        .build()

                    Timber.d("Sending refresh token request")
                    val refreshedToken = executeRefreshTokenRequest(refreshRequest)

                    if (refreshedToken != null) {
                        updateTokenInPrefs(refreshedToken.accessToken, refreshedToken.refreshToken)
                        lastRefreshFailed = false

                        return@withLock originRequest.newBuilder()
                            .header("Authorization", "Bearer ${refreshedToken.accessToken}")
                            .build()
                    } else {
                        Timber.e("Token refresh failed - null response")
                        lastRefreshFailed = true
                        triggerSessionExpiredOnce()
                        return@withLock null
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error during token refresh")
                    lastRefreshFailed = true
                    triggerSessionExpiredOnce()
                    return@withLock null
                }
            }
        }
    }

    private fun triggerSessionExpiredOnce() {
        if (!sessionExpired) {
            sessionExpired = true
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
            if (response.isSuccessful) {
                response.body?.string()?.let {
                    try {
                        Json { ignoreUnknownKeys = true }.decodeFromString(it)
                    } catch (e: Exception) {
                        Timber.e(e, "Token parsing failed")
                        null
                    }
                }
            } else {
                Timber.e("Refresh failed: ${response.code}")
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
        lastRefreshFailed = false
    }

    fun resetSessionExpiredFlag() {
        sessionExpired = false
        lastRefreshFailed = false
    }

    fun getSessionExpiredFlag(): Boolean = sessionExpired
}