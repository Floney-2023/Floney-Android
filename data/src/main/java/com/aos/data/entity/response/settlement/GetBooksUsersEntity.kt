package com.aos.data.entity.response.settlement

import kotlinx.serialization.Serializable

@Serializable
data class GetBooksUsersEntity(
    val email: String,
    val nickname: String,
    val profileImg: String? = "user_default"
)