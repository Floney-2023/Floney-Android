package com.aos.data.entity.response.book

import kotlinx.serialization.Serializable

@Serializable
data class GetBooksEntity (
    val bookName : String,
    val bookImg : String,
    val startDay : String,
    val memberCount : Int,
    val bookKey : String
)