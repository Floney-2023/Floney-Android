package com.aos.data.entity.response.book

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class GetBookCategoryEntity(
    val name: String,
    val categoryKey: String? = null,
    val default: Boolean = false,
    @SerialName("isDefault")
    val isDefault: Boolean? = null,
)
