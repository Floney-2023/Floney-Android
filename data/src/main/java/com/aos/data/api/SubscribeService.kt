package com.aos.data.api

import com.aos.data.entity.request.alarm.PostAlarmSaveBody
import com.aos.data.entity.request.alarm.PostAlarmUpdateBody
import com.aos.data.entity.response.alarm.GetAlarmEntity
import com.aos.data.entity.response.book.GetBookRepeatEntity
import com.aos.data.entity.response.subscribe.GetPresignedUrlEntity
import com.aos.data.entity.response.subscribe.GetSubscribeAndroidEntity
import com.aos.data.entity.response.subscribe.GetSubscribeAndroidInfoEntity
import com.aos.data.entity.response.subscribe.GetSubscribeBenefitEntity
import com.aos.util.NetworkState
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface SubscribeService {
    @GET("subscribe/android/transaction")
    @Headers("Auth: true")
    suspend fun getSubscribeAndroid(
        @Query("transactionId") purchaseToken: String
    ): NetworkState<GetSubscribeAndroidEntity>

    @GET("subscribe")
    @Headers("Auth: true")
    suspend fun getSubscribeCheck(
    ): NetworkState<GetSubscribeAndroidEntity>

    @GET("subscribe/url")
    @Headers("Auth: true")
    suspend fun getPresignedUrl(
        @Query("bookKey") bookKey: String
    ): NetworkState<GetPresignedUrlEntity>

    @GET("subscribe/android/info")
    @Headers("Auth: true")
    suspend fun getSubscribeAndroidInfo(
    ): NetworkState<GetSubscribeAndroidInfoEntity>

    @GET("subscribe/benefit")
    @Headers("Auth: true")
    suspend fun getSubscribeBenefit(
        @Query("bookKey") bookKey: String
    ): NetworkState<GetSubscribeBenefitEntity>
}