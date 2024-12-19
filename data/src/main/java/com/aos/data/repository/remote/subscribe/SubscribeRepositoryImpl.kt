package com.aos.data.repository.remote.subscribe

import com.aos.data.entity.request.analyze.PostAnalyzeAssetBody
import com.aos.data.entity.request.analyze.PostAnalyzeBudgetBody
import com.aos.data.entity.request.analyze.PostAnalyzeCategoryBody
import com.aos.data.mapper.toGetSubscribeAndroidModel
import com.aos.data.mapper.toUiAnalyzeAssetModel
import com.aos.data.mapper.toUiAnalyzeModel
import com.aos.data.mapper.toUiAnalyzePlanModel
import com.aos.data.util.RetrofitFailureStateException
import com.aos.model.analyze.UiAnalyzeAssetModel
import com.aos.model.analyze.UiAnalyzeCategoryInComeModel
import com.aos.model.analyze.UiAnalyzeCategoryOutComeModel
import com.aos.model.analyze.UiAnalyzePlanModel
import com.aos.model.subscribe.GetSubscribeAndroidModel
import com.aos.repository.AnalyzeRepository
import com.aos.repository.SubscribeRepository
import com.aos.util.NetworkState
import timber.log.Timber
import javax.inject.Inject

class SubscribeRepositoryImpl @Inject constructor(private val subscribeRemoteDataSourceImpl: SubscribeRemoteDataSourceImpl) :
    SubscribeRepository {

    override suspend fun getSubscribeAndroid(purchaseToken: String): Result<GetSubscribeAndroidModel> {
        when (val data = subscribeRemoteDataSourceImpl.getSubscribeAndroid(
            purchaseToken = purchaseToken
        )) {
            is NetworkState.Success -> return Result.success(data.body.toGetSubscribeAndroidModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                Timber.e(data.t?.message)
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }

    override suspend fun getSubscribeCheck(device: String): Result<GetSubscribeAndroidModel> {
        when (val data = subscribeRemoteDataSourceImpl.getSubscribeCheck(
            device = device
        )) {
            is NetworkState.Success -> return Result.success(data.body.toGetSubscribeAndroidModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                Timber.e(data.t?.message)
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }
}