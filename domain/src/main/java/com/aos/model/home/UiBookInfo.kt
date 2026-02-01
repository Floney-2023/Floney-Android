package com.aos.model.home


data class UiBookInfoModel(
    val bookImg: String? = "book_default",
    val bookName: String,
    val startDay: String,
    val seeProfileStatus: Boolean,
    val carryOver: String,
    val ourBookUsers: List<OurBookUsers>
)

data class OurBookUsers(
    val name: String,
    val profileImg: String? = "user_default",
    val role: String,
    val me: Boolean
)
