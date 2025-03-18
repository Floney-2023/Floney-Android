package com.aos.repository

import com.aos.model.subscribe.GetPresignedUrlModel
import com.aos.model.subscribe.UiSubscribeAndroidInfoModel
import com.aos.model.subscribe.GetSubscribeAndroidModel
import com.aos.model.subscribe.GetSubscribeBenefitModel
import com.aos.model.subscribe.GetSubscribeUserBenefitModel

interface SubscribeRepository {

    // 구글 안드로이드 트랜잭션(구매) 검증
    suspend fun getSubscribeAndroid(purchaseToken : String): Result<GetSubscribeAndroidModel>

    // 구독 여부 가져오기
    suspend fun getSubscribeCheck(): Result<GetSubscribeAndroidModel>

    // 구독 정보 가져오기
    suspend fun getSubscribeAndroidInfo(): Result<UiSubscribeAndroidInfoModel>

    suspend fun getSubscribeBook(bookKey: String): Result<GetSubscribeAndroidModel>

    // PresignedUrl 가져오기
    suspend fun getPresignedUrl(bookKey: String): Result<GetPresignedUrlModel>

    // 가계부가 구독 혜택을 받고 있는 지 확인
    suspend fun getSubscribeBenefit(bookKey: String): Result<GetSubscribeBenefitModel>

    // 유저가 구독 혜택을 받고 있는 지 확인
    suspend fun getSubscribeUserBenefit(): Result<GetSubscribeUserBenefitModel>

    // s3 (클라우드) 이미지 삭제
    suspend fun deleteCloudImg(id: Int): Result<Void>
}
