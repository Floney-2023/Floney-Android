package com.aos.usecase.history

import com.aos.model.book.PostBooksLinesModel
import com.aos.repository.BookRepository

class PostBooksLinesUseCase(private val bookRepository: BookRepository) {
    suspend operator fun invoke(
       bookKey: String,
       money: Double,
       lineType: String,
       asset:String,
       line: String,
       lineDate: String,
       description: String,
       except: Boolean,
       nickname: String,
       repeatDuration: String,
       memo: String,
       imageUrl: List<String>,
    ): Result<PostBooksLinesModel> {
        return bookRepository.postBooksLines(bookKey, money, lineType, asset, line, lineDate, description, except, nickname, repeatDuration, memo, imageUrl)
    }
}