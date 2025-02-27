package com.aos.data.entity.response.analyze

import kotlinx.serialization.Serializable

@Serializable
data class PostAnalyzeLineSubCategoryEntity(
    val subcategoryName: String,
    val bookLines: List<bookLines>
)

@Serializable
data class bookLines(
    val money: Double,
    val asset: String,
    val lineDate: String,
    val description: String,
    val userProfileImg: String? = ""
)