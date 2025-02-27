package com.aos.data.repository.remote.analyze

import com.aos.data.api.AnalyzeService
import com.aos.data.entity.request.analyze.PostAnalyzeAssetBody
import com.aos.data.entity.request.analyze.PostAnalyzeBudgetBody
import com.aos.data.entity.request.analyze.PostAnalyzeCategoryBody
import com.aos.data.entity.request.analyze.PostAnalyzeLineSubCategoryBody
import com.aos.data.entity.response.analyze.PostAnalyzeAssetEntity
import com.aos.data.entity.response.analyze.PostAnalyzeBudgetEntity
import com.aos.data.entity.response.analyze.PostAnalyzeCategoryInComeEntity
import com.aos.data.entity.response.analyze.PostAnalyzeCategoryOutComeEntity
import com.aos.data.entity.response.analyze.PostAnalyzeLineSubCategoryEntity
import com.aos.util.NetworkState
import javax.inject.Inject

class AnalyzeRemoteDataSourceImpl @Inject constructor(private val analyzeService: AnalyzeService) :
    AnalyzeRemoteDataSource {
    override suspend fun postAnalyzeOutComeCategory(
        body: PostAnalyzeCategoryBody
    ): NetworkState<PostAnalyzeCategoryOutComeEntity> {
        return analyzeService.postAnalyzeOutComeCategory(body)
    }

    override suspend fun postAnalyzeInComeCategory(body: PostAnalyzeCategoryBody): NetworkState<PostAnalyzeCategoryInComeEntity> {
        return analyzeService.postAnalyzeInComeCategory(body)
    }

    override suspend fun postAnalyzeBudget(body: PostAnalyzeBudgetBody): NetworkState<PostAnalyzeBudgetEntity> {
        return analyzeService.postAnalyzeBudget(body)
    }

    override suspend fun postAnalyzeAsset(body: PostAnalyzeAssetBody): NetworkState<PostAnalyzeAssetEntity> {
        return analyzeService.postAnalyzeAsset(body)
    }

    override suspend fun postAnalyzeLineSubCategory(body: PostAnalyzeLineSubCategoryBody): NetworkState<PostAnalyzeLineSubCategoryEntity> {
        return analyzeService.postAnalyzeLineSubCategory(body)
    }
}