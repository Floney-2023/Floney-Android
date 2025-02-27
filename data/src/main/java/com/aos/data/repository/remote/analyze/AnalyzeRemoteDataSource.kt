package com.aos.data.repository.remote.analyze

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

interface AnalyzeRemoteDataSource {

    suspend fun postAnalyzeOutComeCategory(body: PostAnalyzeCategoryBody): NetworkState<PostAnalyzeCategoryOutComeEntity>
    suspend fun postAnalyzeInComeCategory(body: PostAnalyzeCategoryBody): NetworkState<PostAnalyzeCategoryInComeEntity>
    suspend fun postAnalyzeBudget(body: PostAnalyzeBudgetBody): NetworkState<PostAnalyzeBudgetEntity>
    suspend fun postAnalyzeAsset(body: PostAnalyzeAssetBody): NetworkState<PostAnalyzeAssetEntity>
    suspend fun postAnalyzeLineSubCategory(body: PostAnalyzeLineSubCategoryBody): NetworkState<PostAnalyzeLineSubCategoryEntity>
}