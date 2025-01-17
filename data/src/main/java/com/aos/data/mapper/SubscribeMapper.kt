package com.aos.data.mapper

import com.aos.data.entity.response.subscribe.GetPresignedUrlEntity
import com.aos.data.entity.response.subscribe.GetSubscribeAndroidEntity
import com.aos.data.entity.response.subscribe.GetSubscribeAndroidInfoEntity
import com.aos.model.book.CurrencyInform
import com.aos.model.subscribe.GetPresignedUrlModel
import com.aos.model.subscribe.UiSubscribeAndroidInfoModel
import com.aos.model.subscribe.GetSubscribeAndroidModel
import java.text.NumberFormat
import kotlin.math.absoluteValue
import kotlin.math.roundToLong

fun GetSubscribeAndroidEntity.toGetSubscribeAndroidModel(): GetSubscribeAndroidModel {
    return GetSubscribeAndroidModel(this.isValid ?: false)
}

fun GetSubscribeAndroidInfoEntity.toGetSubscribeAndroidInfoModel(): UiSubscribeAndroidInfoModel {
    return UiSubscribeAndroidInfoModel(
        expiryTimeMillis = this.expiryTimeMillis.let { formatDate(it)?.plus(" 만료 예정") },
        autoResumeTimeMillis = this.autoResumeTimeMillis.let { formatDate(this.expiryTimeMillis)?.plus(" 갱신 예정") },
        autoRenewing = this.autoRenewing ?: false,
        priceCurrencyCode = this.priceCurrencyCode.let {
            if (it == "KRW") {
                "₩"  // KRW인 경우 ₩을 반환
            } else {
                getCurrencySymbolByCode(it!!)
            }
        },
        priceAmountMicros = this.priceAmountMicros.let { convertMicrosToCurrency(it) },
        active = this.active,
        remainingDays = calculateRemainingDays(
            startTimeMillis = this.startTimeMillis,
            expiryTimeMillis = this.expiryTimeMillis,
            autoRenewing = this.autoRenewing ?: false
        )
    )
}

fun GetPresignedUrlEntity.toGetPresignedUrlModel(): GetPresignedUrlModel {
    return GetPresignedUrlModel(this.fileName, this.url, this.viewUrl)
}

fun getCurrencySymbolByCode(code: String): String {
    val currencyList = CurrencyInform()
    val currency = currencyList.find { it.code == code }
    return currency?.symbol ?: ""
}

fun convertMicrosToCurrency(micros: String?): String? {
    val amount = micros?.toLongOrNull()?.let { it / 1_000_000.0 } ?: return null
    return NumberFormat.getNumberInstance().format(amount.roundToLong().absoluteValue)
}

fun formatDate(timestamp: String?): String? {
    return timestamp?.toLongOrNull()?.let {
        val sdf = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault())
        sdf.format(java.util.Date(it))
    }
}

fun calculateRemainingDays(
    startTimeMillis: String?,
    expiryTimeMillis: String?,
    autoRenewing: Boolean
): String? {
    val currentTime = System.currentTimeMillis()
    val targetTime = if (autoRenewing) {
        startTimeMillis?.toLongOrNull()
    } else {
        expiryTimeMillis?.toLongOrNull()
    }

    return targetTime?.let {
        val diff = (it - currentTime) / (1000 * 60 * 60 * 24)
        if (autoRenewing) "D+${diff + 1}" else "D-${-diff - 1}"
    }
}