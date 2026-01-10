package com.aos.data.entity.response.subscribe

import kotlinx.serialization.Serializable

@Serializable
data class GetSubscribeUserBenefitEntity (
    val maxBook : Boolean, // 가계부 2개 초과 생성
)