package com.aos.usecase.subscribe

import com.aos.model.subscribe.GetPresignedUrlModel
import com.aos.repository.SubscribeRepository
import javax.inject.Inject

class SubscribePresignedUrlUseCase @Inject constructor(
    private val subscribeRepository: SubscribeRepository
) {
    suspend operator fun invoke(
        bookKey: String
    ): Result<GetPresignedUrlModel> {
        return subscribeRepository.getPresignedUrl(bookKey)
    }
}