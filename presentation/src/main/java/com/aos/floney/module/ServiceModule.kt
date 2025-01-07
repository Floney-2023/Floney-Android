package com.aos.floney.module

import com.aos.data.api.AlarmService
import com.aos.data.api.AnalyzeService
import com.aos.data.api.BookService
import com.aos.data.api.NaverShortenUrlService
import com.aos.data.api.SubscribeService
import com.aos.data.api.UserService
import com.aos.data.util.CustomCallAdapterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserService = retrofit.create(UserService::class.java)

    @Provides
    @Singleton
    fun provideBookService(retrofit: Retrofit): BookService = retrofit.create(BookService::class.java)

    @Provides
    @Singleton
    fun provideAnalyzeService(retrofit: Retrofit): AnalyzeService = retrofit.create(AnalyzeService::class.java)

    @Provides
    @Singleton
    fun provideAlarmService(retrofit: Retrofit): AlarmService = retrofit.create(AlarmService::class.java)


    @Provides
    @Singleton
    fun provideNaverShortenUrlService(okHttpClient: OkHttpClient): NaverShortenUrlService {
        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://openapi.naver.com/v1/util/")
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
        return retrofit.create(NaverShortenUrlService::class.java)
    }
    @Provides
    @Singleton
    fun provideSubscribeService(retrofit: Retrofit): SubscribeService = retrofit.create(SubscribeService::class.java)

}