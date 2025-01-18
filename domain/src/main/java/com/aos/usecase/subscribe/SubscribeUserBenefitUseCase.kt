package com.aos.usecase.subscribe

import com.aos.model.subscribe.GetSubscribeUserBenefitModel
import com.aos.repository.SubscribeRepository
import javax.inject.Inject

class SubscribeUserBenefitUseCase @Inject constructor(
    private val subscribeRepository: SubscribeRepository
) {
    suspend operator fun invoke(
    ): Result<GetSubscribeUserBenefitModel> {
        return subscribeRepository.getSubscribeUserBenefit()
    }
}