package com.aos.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.aos.data.entity.response.subscribe.GetPresignedUrlEntity
import com.aos.data.entity.response.subscribe.GetSubscribeAndroidEntity
import com.aos.data.entity.response.subscribe.GetSubscribeAndroidInfoEntity
import com.aos.data.entity.response.subscribe.GetSubscribeBenefitEntity
import com.aos.data.entity.response.subscribe.GetSubscribeUserBenefitEntity
import com.aos.model.book.CurrencyInform
import com.aos.model.subscribe.GetPresignedUrlModel
import com.aos.model.subscribe.UiSubscribeAndroidInfoModel
import com.aos.model.subscribe.GetSubscribeAndroidModel
import com.aos.model.subscribe.GetSubscribeBenefitModel
import com.aos.model.subscribe.GetSubscribeUserBenefitModel
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
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


fun GetSubscribeBenefitEntity.toGetSubscribeBenefitModel(): GetSubscribeBenefitModel {
    return GetSubscribeBenefitModel(this.maxFavorite, this.overBookUser)
}

fun GetSubscribeUserBenefitEntity.toGetSubscribeUserBenefitModel(): GetSubscribeUserBenefitModel {
    return GetSubscribeUserBenefitModel(this.maxBook)
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
    val currentDate = System.currentTimeMillis()

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        // SDK 26 미만
        if (autoRenewing) {
            startTimeMillis?.toLongOrNull()?.let { start ->
                val startDate = start / (1000 * 60 * 60 * 24)
                val todayDate = currentDate / (1000 * 60 * 60 * 24)
                val diff = (todayDate - startDate + 1).toInt()
                return "D+$diff"
            }
        } else {
            expiryTimeMillis?.toLongOrNull()?.let { expiry ->
                val expiryDate = expiry / (1000 * 60 * 60 * 24)
                val todayDate = currentDate / (1000 * 60 * 60 * 24)
                val diff = (expiryDate - todayDate).toInt()
                return when {
                    diff > 0 -> "D-$diff"
                    diff == 0 -> "D-DAY"
                    else -> null
                }
            }
        }
        null // fallback
    } else {
        // SDK 26 이상
        val zone = ZoneId.systemDefault()

        if (autoRenewing) {
            startTimeMillis?.toLongOrNull()?.let { start ->
                val startDate = Instant.ofEpochMilli(start).atZone(zone).toLocalDate()
                val todayDate = Instant.now().atZone(zone).toLocalDate()
                val diff = ChronoUnit.DAYS.between(startDate, todayDate).toInt() + 1
                return "D+$diff"
            }
        } else {
            expiryTimeMillis?.toLongOrNull()?.let { expiry ->
                val expiryDate = Instant.ofEpochMilli(expiry).atZone(zone).toLocalDate()
                val todayDate = Instant.now().atZone(zone).toLocalDate()
                val diff = ChronoUnit.DAYS.between(todayDate, expiryDate).toInt()
                return when {
                    diff > 0 -> "D-$diff"
                    diff == 0 -> "D-DAY"
                    else -> null
                }
            }
        }
        null // fallback
    }
}
