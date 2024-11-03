package com.aos.repository

import com.aos.model.subscribe.GetSubscribeAndroidModel

interface SubscribeRepository {

    // 분석 - 지출 가져오기
    suspend fun getSubsctibeAndroid(purchaseToken : String): Result<GetSubscribeAndroidModel>

}