package com.aos.data.repository.remote.subscribe

import com.aos.data.entity.request.analyze.PostAnalyzeAssetBody
import com.aos.data.entity.request.analyze.PostAnalyzeBudgetBody
import com.aos.data.entity.request.analyze.PostAnalyzeCategoryBody
import com.aos.data.entity.response.analyze.PostAnalyzeAssetEntity
import com.aos.data.entity.response.analyze.PostAnalyzeBudgetEntity
import com.aos.data.entity.response.analyze.PostAnalyzeCategoryInComeEntity
import com.aos.data.entity.response.analyze.PostAnalyzeCategoryOutComeEntity
import com.aos.data.entity.response.subscribe.GetPresignedUrlEntity
import com.aos.data.entity.response.subscribe.GetSubscribeAndroidEntity
import com.aos.data.entity.response.subscribe.GetSubscribeAndroidInfoEntity
import com.aos.util.NetworkState

interface SubscribeRemoteDataSource {
    suspend fun getSubscribeAndroid(purchaseToken : String): NetworkState<GetSubscribeAndroidEntity>

    suspend fun getSubscribeCheck(): NetworkState<GetSubscribeAndroidEntity>

    suspend fun getSubscribeAndroidInfo(): NetworkState<GetSubscribeAndroidInfoEntity>

    suspend fun getPresignedUrl(bookKey: String): NetworkState<GetPresignedUrlEntity>
}