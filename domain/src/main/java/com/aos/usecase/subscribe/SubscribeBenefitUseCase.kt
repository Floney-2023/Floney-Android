package com.aos.usecase.subscribe

import com.aos.model.subscribe.GetSubscribeBenefitModel
import com.aos.repository.SubscribeRepository
import javax.inject.Inject

class SubscribeBenefitUseCase @Inject constructor(
    private val subscribeRepository: SubscribeRepository
) {
    suspend operator fun invoke(
        bookKey: String
    ): Result<GetSubscribeBenefitModel> {
        return subscribeRepository.getSubscribeBenefit(bookKey)
    }
}