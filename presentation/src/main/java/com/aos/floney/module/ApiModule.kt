package com.aos.floney.module

import android.content.Context
import android.content.SharedPreferences
import com.aos.data.BuildConfig
import com.aos.data.api.NaverShortenUrlService
import com.aos.data.util.AuthInterceptor
import com.aos.data.util.CustomCallAdapterFactory
import com.aos.data.util.HeaderInterceptor
import com.aos.data.util.SharedPreferenceUtil
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Singleton
    @Provides
    fun provideSharedPreferenceUtil(@ApplicationContext context: Context): SharedPreferenceUtil = SharedPreferenceUtil(context)

    @Singleton
    @Provides
    fun provideHeaderInterceptor(prefs: SharedPreferenceUtil): HeaderInterceptor {
        return HeaderInterceptor(prefs)
    }

    @Singleton
    @Provides
    fun provideAuthInterceptor(prefs: SharedPreferenceUtil): AuthInterceptor {
        return AuthInterceptor(prefs)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        headerInterceptor: HeaderInterceptor,
        authInterceptor: AuthInterceptor
    ) = run {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(headerInterceptor)
            .authenticator(authInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = run {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(com.aos.floney.BuildConfig.BASE_URL)
            .addCallAdapterFactory(CustomCallAdapterFactory())
            .addConverterFactory(
                Json {
                    isLenient = true
                    ignoreUnknownKeys = true // 지정되지 않은 key 값은 무시
                    coerceInputValues = true // default 값 설정
                    explicitNulls = false // 없는 필드는 null로 설정
                }.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }
}