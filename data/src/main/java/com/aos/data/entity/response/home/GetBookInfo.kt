package com.aos.data.entity.response.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetBookInfoEntity(
    val bookImg: String? = "",
    val bookName: String,
    val startDay: String,
    val seeProfileStatus: Boolean,
    val carryOver: String,
    val ourBookUsers: List<OurBookUsers>
)

@Serializable
data class OurBookUsers(
    val name: String,
    val profileImg: String? = "user_default",
    val role: String,
    val me: Boolean
)
