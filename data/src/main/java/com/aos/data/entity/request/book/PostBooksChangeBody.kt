package com.aos.data.entity.request.book

import kotlinx.serialization.Serializable

@Serializable
data class PostBooksChangeBody (
    val lineId: Int,
    val bookKey: String,
    val money: Double,
    val lineType: String,
    val asset:String,
    val line: String,
    val lineDate: String,
    val description: String,
    val except: Boolean,
    val nickname: String,
    val memo : String,
    val imageUrl : List<String>
)