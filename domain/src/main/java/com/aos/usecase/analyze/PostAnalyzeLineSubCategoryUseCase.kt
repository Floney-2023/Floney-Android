package com.aos.usecase.analyze

import com.aos.model.analyze.UiAnalyzeAssetModel
import com.aos.model.analyze.UiAnalyzeLineSubCategoryModel
import com.aos.repository.AnalyzeRepository
import javax.inject.Inject

class PostAnalyzeLineSubCategoryUseCase @Inject constructor(private val analyzeRepository: AnalyzeRepository) {

    suspend operator fun invoke(
        bookKey: String,
        category: String,
        subcategory: String,
        emails: List<String>,
        sortingType: String = "EVERYDAY",
        yearMonth: String
    ): Result<UiAnalyzeLineSubCategoryModel> {
        return analyzeRepository.postAnalyzeLineSubCategory(bookKey, category, subcategory, emails, sortingType, yearMonth)
    }

}