package com.aos.data.entity.request.book

import kotlinx.serialization.Serializable

@Serializable
data class PostBooksCreateBody(
    val name: String,
    val profileImg: String,
    val requestCurrency: String
)
