package com.aos.data.entity.response.book

import kotlinx.serialization.Serializable

@Serializable
data class GetBookCategoryEntity(
    val name: String,
    val categoryKey: String? = null,
    val default: Boolean
)
