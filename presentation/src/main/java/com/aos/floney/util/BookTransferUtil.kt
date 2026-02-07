package com.aos.floney.util

import androidx.annotation.StringRes
import com.aos.floney.R


enum class BookCategory(@StringRes val labelRes: Int) {
    ASSET(R.string.book_setting_category_add_assets),
    EXPENSE(R.string.book_setting_category_add_expense),
    INCOME(R.string.book_setting_category_add_income),
    TRANSFER(R.string.book_setting_category_add_transfer);

    companion object {
        fun fromRaw(value: String?): BookCategory? {
            return when (value?.trim()) {
                // 영어 코드
                "ASSET" -> ASSET
                "EXPENSE" -> EXPENSE
                "INCOME" -> INCOME
                "TRANSFER" -> TRANSFER

                // 서버가 주는 한글
                "자산" -> ASSET
                "지출" -> EXPENSE
                "수입" -> INCOME
                "이체" -> TRANSFER

                else -> null
            }
        }
    }
}
