package com.aos.data.repository.remote.subscribe

import com.aos.data.api.AnalyzeService
import com.aos.data.api.SubscribeService
import com.aos.data.entity.request.analyze.PostAnalyzeAssetBody
import com.aos.data.entity.request.analyze.PostAnalyzeBudgetBody
import com.aos.data.entity.request.analyze.PostAnalyzeCategoryBody
import com.aos.data.entity.response.analyze.PostAnalyzeAssetEntity
import com.aos.data.entity.response.analyze.PostAnalyzeBudgetEntity
import com.aos.data.entity.response.analyze.PostAnalyzeCategoryInComeEntity
import com.aos.data.entity.response.analyze.PostAnalyzeCategoryOutComeEntity
import com.aos.data.entity.response.subscribe.GetSubscribeAndroidEntity
import com.aos.util.NetworkState
import javax.inject.Inject

class SubscribeRemoteDataSourceImpl @Inject constructor(private val subscribeService: SubscribeService) :
    SubscribeRemoteDataSource {
    override suspend fun getSubscribeAndroid(purchaseToken: String): NetworkState<GetSubscribeAndroidEntity> {
        return subscribeService.getSubscribeAndroid(purchaseToken)
    }

    override suspend fun getSubscribeCheck(device: String): NetworkState<GetSubscribeAndroidEntity> {
        return subscribeService.getSubscribeCheck(device)
    }
}