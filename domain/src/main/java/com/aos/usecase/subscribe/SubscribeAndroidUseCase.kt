package com.aos.usecase.subscribe

import com.aos.model.book.PostBooksCreateModel
import com.aos.model.book.PostBooksJoinModel
import com.aos.model.subscribe.GetSubscribeAndroidModel
import com.aos.repository.BookRepository
import com.aos.repository.SubscribeRepository
import javax.inject.Inject

class SubscribeAndroidUseCase @Inject constructor(
    private val subscribeRepository: SubscribeRepository
){
    suspend operator fun invoke(
        purchaseToken : String
    ): Result<GetSubscribeAndroidModel> {
        return subscribeRepository.getSubsctibeAndroid(purchaseToken)
    }

}