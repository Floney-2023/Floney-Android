package com.aos.usecase.subscribe

import com.aos.model.subscribe.GetSubscribeUserBenefitModel
import com.aos.repository.SubscribeRepository
import javax.inject.Inject

class SubscribeDeleteCloudImageUseCase @Inject constructor(
    private val subscribeRepository: SubscribeRepository
) {
    suspend operator fun invoke(
        id : Int
    ): Result<Void?> {
        return subscribeRepository.deleteCloudImg(id)
    }
}