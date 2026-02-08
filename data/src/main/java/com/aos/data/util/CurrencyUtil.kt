package com.aos.data.util

import android.content.Context
import com.aos.model.book.CurrencyInform
import timber.log.Timber
import javax.inject.Inject

object CurrencyUtil {
    var currency = ""

    fun getCurrencyCode(context: Context): String {
        val locale = context.resources.configuration.locales[0]
        val language = locale.language
        val country = locale.country

        return when {
            language == "ko" -> "KRW"
            language == "ja" -> "JPY"
            language == "zh" && country == "CN" -> "CNY"
            language == "vi" -> "VND"
            language == "th" -> "THB"
            language == "id" -> "IDR"
            language == "ms" -> "MYR"
            language == "tr" -> "TRY"
            language == "ru" -> "RUB"
            language == "hu" -> "HUF"
            language == "pl" -> "PLN"
            language == "cs" -> "CZK"
            language == "da" -> "DKK"
            language == "hi" -> "INR"
            language == "en" && country == "GB" -> "GBP"
            else -> "USD" // 🌍 기본 fallback
        }
    }
}

fun getCurrencyCodeBySymbol(symbol: String): String {
    val currencyList = CurrencyInform()
    val currency = currencyList.find { it.symbol == symbol }
    return currency?.code ?: ""
}

fun checkDecimalPoint(): Boolean {
    val currencyCode = getCurrencyCodeBySymbol(CurrencyUtil.currency)
    val noDecimalCurrencies = setOf("KRW", "JPY", "CNY", "VND")
    return !noDecimalCurrencies.contains(currencyCode)
}

