package com.aos.model.subscribe

data class GetPresignedUrlModel(
    val fileName: String,
    val url: String,
    val viewUrl: String
)