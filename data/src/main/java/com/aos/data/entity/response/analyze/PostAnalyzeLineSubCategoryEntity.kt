package com.aos.data.entity.response.analyze

import kotlinx.serialization.Serializable

@Serializable
data class PostAnalyzeLineSubCategoryEntity(
    val subcategoryName: String,
    val bookLines: List<BookLines>
)

@Serializable
data class BookLines(
    val money: Double,
    val asset: String,
    val lineDate: String,
    val description: String,
    val userProfileImg: String? = ""
)