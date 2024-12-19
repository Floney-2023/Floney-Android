package com.aos.repository

import com.aos.model.subscribe.GetSubscribeAndroidModel

interface SubscribeRepository {

    // 구글 안드로이드 트랜잭션(구매) 검증
    suspend fun getSubscribeAndroid(purchaseToken : String): Result<GetSubscribeAndroidModel>

    // 구독 여부 가져오기
    suspend fun getSubscribeCheck(device : String): Result<GetSubscribeAndroidModel>
}