package com.aos.data.entity.response.subscribe

import kotlinx.serialization.Serializable

@Serializable
data class GetSubscribeBenefitEntity (
    val maxFavorite : Boolean, // 즐겨찾기 개수 무제한
    val overBookUser : Boolean // 가계부 인원 4명 초과
)