package com.aos.repository

import com.aos.model.analyze.UiAnalyzeAssetModel
import com.aos.model.analyze.UiAnalyzeCategoryInComeModel
import com.aos.model.analyze.UiAnalyzeCategoryOutComeModel
import com.aos.model.analyze.UiAnalyzeLineSubCategoryModel
import com.aos.model.analyze.UiAnalyzePlanModel

interface AnalyzeRepository {

    // 분석 - 지출 가져오기
    suspend fun postAnalyzeOutComeCategory(bookKey: String, root: String, date: String): Result<UiAnalyzeCategoryOutComeModel>

    // 분석 - 수입 가져오기
    suspend fun postAnalyzeInComeCategory(bookKey: String, root: String, date: String): Result<UiAnalyzeCategoryInComeModel>

    // 분석 - 예산
    suspend fun postAnalyzeBudget(bookKey: String, date: String): Result<UiAnalyzePlanModel>

    // 분석 - 자산
    suspend fun postAnalyzeAsset(bookKey: String, date: String): Result<UiAnalyzeAssetModel>

    // 분석 - 지출/수입 상세 정보
    suspend fun postAnalyzeLineSubCategory(
        bookKey: String,
        category: String,
        subcategory: String,
        emails : List<String>,
        sortingType : String,
        yearMonth : String): Result<UiAnalyzeLineSubCategoryModel>
}