package com.aos.model.book

import androidx.recyclerview.widget.DiffUtil


data class UiBookCurrencyModel(
    val currencyList: List<Currency>
){
    interface OnItemClickListener {
        fun onItemClick(item: Currency)
    }

    companion object : DiffUtil.ItemCallback<Currency>() {
        override fun areItemsTheSame(oldItem: Currency, newItem: Currency): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: Currency, newItem: Currency): Boolean {
            return oldItem == newItem
        }
    }
}
data class Currency(
    val symbol: String,
    val code: String,
    val isCheck : Boolean
)

fun CurrencyInform(): List<Currency> {
    return listOf(
        Currency("원", "KRW", false),
        Currency("$", "USD", false),
        Currency("€", "EUR", false),
        Currency("¥", "JPY", false),
        Currency("¥", "CNY", false),
        Currency("£", "GBP", false),
        Currency("đ", "VND", false),
        Currency("₱", "PHP", false),
        Currency("฿", "THB", false),
        Currency("Rp", "IDR", false),
        Currency("RM", "MYR", false),
        Currency("₺", "TRY", false),
        Currency("₽", "RUB", false),
        Currency("Ft", "HUF", false),
        Currency("Fr", "CHF", false),
        Currency("₹", "INR", false),
        Currency("zł", "PLN", false),
        Currency("Kč", "CZK", false),
        Currency("kr", "DKK", false),
        Currency("₦", "NGN", false),
        Currency("₴", "UAH", false),
    )
}

fun getCurrencySymbolByCode(code: String): String {
    val currencyList = CurrencyInform()
    val currency = currencyList.find { it.code == code }
    return currency?.symbol ?: ""
}

