package com.aos.data.repository.remote.alarm

import android.content.Context
import com.aos.data.entity.request.alarm.PostAlarmSaveBody
import com.aos.data.entity.request.alarm.PostAlarmUpdateBody
import com.aos.data.entity.request.analyze.PostAnalyzeAssetBody
import com.aos.data.entity.request.analyze.PostAnalyzeBudgetBody
import com.aos.data.entity.request.analyze.PostAnalyzeCategoryBody
import com.aos.data.entity.request.book.PostBooksChangeBody
import com.aos.data.mapper.toPostBooksLinesChangeModel
import com.aos.data.mapper.toUiAlarmGetEntity
import com.aos.data.mapper.toUiAnalyzeAssetModel
import com.aos.data.mapper.toUiAnalyzeModel
import com.aos.data.mapper.toUiAnalyzePlanModel
import com.aos.data.util.RetrofitFailureStateException
import com.aos.model.alarm.UiAlarmGetModel
import com.aos.model.analyze.UiAnalyzeAssetModel
import com.aos.model.analyze.UiAnalyzeCategoryInComeModel
import com.aos.model.analyze.UiAnalyzeCategoryOutComeModel
import com.aos.model.analyze.UiAnalyzePlanModel
import com.aos.repository.AlarmRepository
import com.aos.repository.AnalyzeRepository
import com.aos.util.NetworkState
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val alarmRemoteDataSourceImpl: AlarmRemoteDataSourceImpl,
    @ApplicationContext private val context: Context,
) :
    AlarmRepository {

    override suspend fun getAlarm(
        bookKey: String
    ): Result<List<UiAlarmGetModel>> {
        when (val data = alarmRemoteDataSourceImpl.getAlarm(bookKey)) {
            is NetworkState.Success -> return Result.success(data.body.toUiAlarmGetEntity(context))
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
    override suspend fun postAlarmUpdate(
        bookKey: String,
        id : Int
    ): Result<Void?> {
        when (val data = alarmRemoteDataSourceImpl.postAlarmUpdate(bookKey, PostAlarmUpdateBody(id, bookKey))) {
            is NetworkState.Success -> return Result.success(data.body)
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                return if(data.errorState == "body값이 null로 넘어옴") {
                    Result.success(null)
                } else {
                    Result.failure(IllegalStateException("unKnownError"))
                }
            }
        }
    }
    override suspend fun postAlarmSave(
        bookKey: String,
        title: String,
        body: String,
        imgUrl: String,
        userEmail: String,
        date: String
    ): Result<Void?> {
        when (val data = alarmRemoteDataSourceImpl.postAlarmSave(PostAlarmSaveBody(bookKey, title, body, imgUrl, userEmail, date))) {
            is NetworkState.Success -> return Result.success(data.body)
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                return if(data.errorState == "body값이 null로 넘어옴") {
                    Result.success(null)
                } else {
                    Result.failure(IllegalStateException("unKnownError"))
                }
            }
        }
    }
}
