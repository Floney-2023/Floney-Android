package com.aos.usecase.subscribe

import com.aos.model.subscribe.UiSubscribeAndroidInfoModel
import com.aos.repository.SubscribeRepository
import javax.inject.Inject

class SubscribeAndroidInfoUseCase @Inject constructor(
    private val subscribeRepository: SubscribeRepository
){
    suspend operator fun invoke(): Result<UiSubscribeAndroidInfoModel> {
        return subscribeRepository.getSubscribeAndroidInfo()
    }
}