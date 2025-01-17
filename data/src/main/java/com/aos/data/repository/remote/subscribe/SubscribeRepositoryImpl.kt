package com.aos.data.repository.remote.subscribe

import com.aos.data.mapper.toGetPresignedUrlModel
import com.aos.data.mapper.toGetSubscribeAndroidInfoModel
import com.aos.data.mapper.toGetSubscribeAndroidModel
import com.aos.data.util.RetrofitFailureStateException
import com.aos.model.subscribe.GetPresignedUrlModel
import com.aos.model.subscribe.UiSubscribeAndroidInfoModel
import com.aos.model.subscribe.GetSubscribeAndroidModel
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

    override suspend fun getSubscribeCheck(): Result<GetSubscribeAndroidModel> {
        when (val data = subscribeRemoteDataSourceImpl.getSubscribeCheck()) {
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

    override suspend fun getSubscribeAndroidInfo(): Result<UiSubscribeAndroidInfoModel> {
        when (val data = subscribeRemoteDataSourceImpl.getSubscribeAndroidInfo()) {
            is NetworkState.Success -> return Result.success(data.body.toGetSubscribeAndroidInfoModel())
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

    override suspend fun getPresignedUrl(bookKey: String): Result<GetPresignedUrlModel> {
        when (val data = subscribeRemoteDataSourceImpl.getPresignedUrl(bookKey)) {
            is NetworkState.Success -> return Result.success(data.body.toGetPresignedUrlModel())
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