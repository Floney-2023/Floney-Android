package com.aos.data.entity.request.analyze

import kotlinx.serialization.Serializable

@Serializable
data class PostAnalyzeLineSubCategoryBody (
    val bookKey: String,
    val category: String,
    val subcategory: String,
    val emails : List<String>,
    val sortingType : String,
    val yearMonth : String
)