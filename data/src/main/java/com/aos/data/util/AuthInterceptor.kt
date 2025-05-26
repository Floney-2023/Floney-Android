package com.aos.data.util

import com.aos.data.BuildConfig
import com.aos.data.entity.response.token.PostUserReissueEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val prefs: SharedPreferenceUtil
) : okhttp3.Authenticator {

    // 세션 만료 이벤트를 전달하기 위한 SharedFlow
    private val _sessionExpiredEvent = MutableSharedFlow<Boolean>()
    val sessionExpiredEvent: SharedFlow<Boolean> = _sessionExpiredEvent

    // 토큰 갱신을 위한 Mutex
    private val refreshTokenMutex = Mutex()

    // 마지막으로 갱신된 토큰을 캐싱
    @Volatile private var lastRefreshedAccessToken: String? = null

    @Volatile private var sessionExpired = false

    override fun authenticate(route: Route?, response: Response): Request? {
        if (sessionExpired) return null

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

        return runBlocking(Dispatchers.IO) {

            // 이미 성공적으로 토큰이 갱신된 경우 → 재사용
            lastRefreshedAccessToken?.let { token ->
                return@runBlocking originRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            }


            refreshTokenMutex.withLock {
                if (sessionExpired) return@runBlocking null

                // Mutex 안에 다시 한 번 체크
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

                    Timber.d("Sending refresh token request")

                    val refreshedToken = executeRefreshTokenRequest(refreshRequest)

                    refreshedToken?.let {
                        updateTokenInPrefs(it.accessToken, it.refreshToken)

                        // 캐시 업데이트
                        lastRefreshedAccessToken = it.accessToken

                        Timber.d("Success to refresh token")
                        originRequest.newBuilder()
                            .header("Authorization", "Bearer ${it.accessToken}")
                            .build()
                    } ?: run {
                        Timber.e("Failed to refresh token, token response is null")
                        lastRefreshedAccessToken = null
                        notifySessionExpired()
                        null
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error during token refresh")
                    lastRefreshedAccessToken = null
                    notifySessionExpired()
                    null
                }
            }
        }
    }

    private fun notifySessionExpired() {
        sessionExpired = true
        CoroutineScope(Dispatchers.IO).launch {
            _sessionExpiredEvent.emit(true)
        }
    }

    private fun executeRefreshTokenRequest(refreshRequest: Request): PostUserReissueEntity? {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val response = client.newCall(refreshRequest).execute()
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
                Timber.d("Token refresh failed with code: ${response.code}")
                Timber.d("Token refresh failed with message: ${response.message}")
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

    fun clearTokens() {
        prefs.setString("accessToken", "")
        prefs.setString("refreshToken", "")
        lastRefreshedAccessToken = null
    }

    private fun createTokenReissueRequestBody(): RequestBody {
        val accessToken = prefs.getString("accessToken", "")
        val refreshToken = prefs.getString("refreshToken", "")

        val requestBodyString = """
        {
            "accessToken": "$accessToken",
            "refreshToken": "$refreshToken"
        }
        """.trimIndent()

        Timber.d(requestBodyString)

        return requestBodyString.toRequestBody("application/json".toMediaTypeOrNull())
    }

    fun resetSessionExpiredFlag() {
        sessionExpired = false
    }

    fun getSessionExpiredFlag(): Boolean {
        return sessionExpired
    }
}