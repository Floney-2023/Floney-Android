package com.aos.usecase.history

import com.aos.model.book.PostBooksChangeModel
import com.aos.model.book.PostBooksLinesModel
import com.aos.repository.BookRepository

class PostBooksLinesChangeUseCase(private val bookRepository: BookRepository) {
    suspend operator fun invoke(
       lineId: Int,
       bookKey: String,
       money: Double,
       flow: String,
       asset:String,
       line: String,
       lineDate: String,
       description: String,
       except: Boolean,
       nickname: String,
       memo : String,
       imageUrls : List<String>
    ): Result<PostBooksChangeModel> {
        return bookRepository.postBooksLinesChange(lineId, bookKey, money, flow, asset, line, lineDate, description, except, nickname, memo, imageUrls)
    }
}