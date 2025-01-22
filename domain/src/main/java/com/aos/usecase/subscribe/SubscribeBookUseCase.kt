package com.aos.usecase.subscribe

import com.aos.model.subscribe.GetSubscribeAndroidModel
import com.aos.repository.SubscribeRepository
import javax.inject.Inject

class SubscribeBookUseCase @Inject constructor(
    private val subscribeRepository: SubscribeRepository
){
    suspend operator fun invoke(
        bookKey : String
    ): Result<GetSubscribeAndroidModel> {
        return subscribeRepository.getSubscribeBook(bookKey)
    }

}