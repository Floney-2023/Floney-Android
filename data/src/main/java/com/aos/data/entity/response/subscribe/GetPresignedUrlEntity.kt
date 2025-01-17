package com.aos.data.entity.response.subscribe

import kotlinx.serialization.Serializable

@Serializable
data class GetPresignedUrlEntity(
    val fileName: String,
    val url: String,
    val viewUrl: String
)
