package com.aos.data.entity.response.book

import kotlinx.serialization.Serializable

@Serializable
data class PostBooksChangeEntity (
    val money: Double,
    val lineType: String,
    val asset:String,
    val line: String,
    val lineDate: String,
    val description: String,
    val except: Boolean,
    val nickname: String,
)