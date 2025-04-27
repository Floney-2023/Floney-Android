package com.aos.data.entity.response.book

import kotlinx.serialization.Serializable

@Serializable
data class GetBooksInfoEntity(
    val bookImg: String? = "",
    val bookName: String,
    val startDay: String,
    val seeProfileStatus: Boolean,
    val carryOver: Boolean,
    val ourBookUsers: List<BookUser>
)

@Serializable
data class BookUser(
    val name: String,
    val profileImg: String ?= "user_default",
    val email: String,
    val me: Boolean,
    val role: String
)