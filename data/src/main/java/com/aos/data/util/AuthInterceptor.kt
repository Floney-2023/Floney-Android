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
    @Volatile private var lastRefreshedAccessToken: String? = null
    @Volatile private var sessionExpired = false

    override fun authenticate(route: Route?, response: Response): Request? {
        val originRequest = response.request

        // 이미 재발급 요청이면 루프 방지
        if (originRequest.url.encodedPath.endsWith("/users/reissue")) return null
        if (sessionExpired) return null

        if (originRequest.header("Authorization").isNullOrEmpty()) return null

        val refreshToken = prefs.getString("refreshToken", "")
        if (refreshToken.isBlank()) {
            Timber.e("Refresh token is empty.")
            notifySessionExpired()
            return null
        }

        return runBlocking(Dispatchers.IO) {
            // 기존 캐시된 토큰 재사용
            lastRefreshedAccessToken?.let { token ->
                return@runBlocking originRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            }

            refreshTokenMutex.withLock {
                // 재확인
                if (sessionExpired) return@runBlocking null

                lastRefreshedAccessToken?.let { token ->
                    Timber.d("Reusing already refreshed token")
                    return@withLock originRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                }

                try {
                    val refreshRequest = Request.Builder()
                        .url("${BuildConfig.BASE_URL}users/reissue")
                        .post(createTokenReissueRequestBody())
                        .build()

                    val refreshedToken = executeRefreshTokenRequest(refreshRequest)

                    refreshedToken?.let {
                        updateTokenInPrefs(it.accessToken, it.refreshToken)
                        lastRefreshedAccessToken = it.accessToken
                        sessionExpired = false

                        Timber.d("Token refreshed successfully")
                        originRequest.newBuilder()
                            .header("Authorization", "Bearer ${it.accessToken}")
                            .build()
                    } ?: run {
                        Timber.e("Token refresh failed or returned null")
                        lastRefreshedAccessToken = null
                        notifySessionExpired()
                        null
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Exception during token refresh")
                    lastRefreshedAccessToken = null
                    notifySessionExpired()
                    null
                }
            }
        }
    }

    private fun executeRefreshTokenRequest(request: Request): PostUserReissueEntity? {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        return client.newCall(request).execute().use { response ->
            if (response.code == 204 || !response.isSuccessful || response.body == null) {
                Timber.e("Token refresh failed. Code: ${response.code}")
                return null
            }

            val json = Json { ignoreUnknownKeys = true }
            val body = response.body!!.string()
            return try {
                json.decodeFromString<PostUserReissueEntity>(body)
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse refresh token response")
                null
            }
        }
    }

    private fun createTokenReissueRequestBody(): RequestBody {
        val accessToken = prefs.getString("accessToken", "")
        val refreshToken = prefs.getString("refreshToken", "")

        val bodyString = """
            {
                "accessToken": "$accessToken",
                "refreshToken": "$refreshToken"
            }
        """.trimIndent()

        Timber.d("Refresh Request Body: $bodyString")
        return bodyString.toRequestBody("application/json".toMediaTypeOrNull())
    }

    private fun updateTokenInPrefs(accessToken: String, refreshToken: String) {
        if (accessToken.isBlank() || refreshToken.isBlank()) {
            Timber.e("Empty token received")
            return
        }
        prefs.setString("accessToken", accessToken)
        prefs.setString("refreshToken", refreshToken)
    }

    private fun notifySessionExpired() {
        sessionExpired = true
        CoroutineScope(Dispatchers.IO).launch {
            _sessionExpiredEvent.emit(true)
        }
    }

    fun clearTokens() {
        prefs.setString("accessToken", "")
        prefs.setString("refreshToken", "")
        lastRefreshedAccessToken = null
    }

    fun resetSessionExpiredFlag() {
        sessionExpired = false
    }

    fun getSessionExpiredFlag(): Boolean = sessionExpired
}