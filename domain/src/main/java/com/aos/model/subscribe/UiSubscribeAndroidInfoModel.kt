package com.aos.model.subscribe


data class UiSubscribeAndroidInfoModel(
    val expiryTimeMillis: String?, // 정기 결제 만료 시간(구독 종료일)
    val autoResumeTimeMillis: String?, // 정기 결제 갱신 시간(구독 갱신일)
    val autoRenewing: Boolean, // 정기 결제가 만료 시간에 도달했을 때 자동으로 갱신되는 여부
    val priceCurrencyCode: String?, // 정기 결제 가격의 통화 코드
    val priceAmountMicros: String?, // 정기 결제 가격 (마이크로 단위)
    val active: Boolean, // 정기 결제 활성화 여부
    val remainingDays: String? // 남은 일수 계산 (D+1 / D-1)
)