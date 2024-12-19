package com.aos.usecase.subscribe

import com.aos.model.subscribe.GetSubscribeAndroidModel
import com.aos.repository.SubscribeRepository
import javax.inject.Inject

class SubscribeCheckUseCase @Inject constructor(
    private val subscribeRepository: SubscribeRepository
){
    suspend operator fun invoke(
        device : String
    ): Result<GetSubscribeAndroidModel> {
        return subscribeRepository.getSubscribeCheck(device)
    }

}