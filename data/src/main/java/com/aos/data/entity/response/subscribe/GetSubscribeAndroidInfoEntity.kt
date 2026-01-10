package com.aos.data.entity.response.subscribe

import kotlinx.serialization.Serializable

@Serializable
data class GetSubscribeAndroidInfoEntity (
    val id : Int,
    val paymentState : String?, // 결제 상태 : 0(결제 대기 중), 1(결제 수신), 2(무료 체험), 3(업그레이드)
    val expiryTimeMillis : String?, // 구독 만료 시간
    val cancelReason : String?, // 정기 결제 취소 이유, 0(사용자 취소), 1(결제 문제), 2(새 정기 결제로 교체), 3(개발자가 정기 결제 취소)
    val orderId : String?, // 최근 반복 주문의 ID
    val startTimeMillis : String?, // 정기 결제 승인 시간
    val autoResumeTimeMillis : String?, // 구독 만료 후, 자동 갱신이 시작되는 시간
    val autoRenewing : Boolean?, // 정기 결제가 만료 시간에 도달했을 때 자동으로 갱신되는 여부
    val priceCurrencyCode : String?, // 정기 결제 가격의 통화 코드
    val priceAmountMicros : String?, // 정기 결제 가격 (마이크로 단위)
    val active: Boolean // 활성화 여부
)