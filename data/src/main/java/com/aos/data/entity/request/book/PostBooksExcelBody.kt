package com.aos.data.entity.request.book

import kotlinx.serialization.Serializable
import retrofit2.http.Query

@Serializable
data class PostBooksExcelBody (
    val bookKey: String,
    val excelDuration: String,
    val currentDate: String,
    val language: String? = null
)
