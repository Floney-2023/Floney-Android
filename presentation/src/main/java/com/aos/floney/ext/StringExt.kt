package com.aos.floney.ext

import androidx.annotation.StringRes
import com.aos.data.util.CurrencyUtil
import com.aos.floney.BuildConfig.appsflyer_settlement_url
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.di.AppApplication
import org.json.JSONObject

private const val NETWORK_ERROR = "NetworkError"
private const val UNKNOWN_ERROR = "unKnownError"
private const val EXPIRED_TOKEN_KO = "만료된 토큰입니다"

private fun localizedString(@StringRes resId: Int): String {
    return runCatching { AppApplication.instance.getString(resId) }.getOrDefault("")
}

fun String?.parseErrorMsg(event: BaseViewModel? = null): String {
    return if (this == "") {
        ""
    } else if (this == NETWORK_ERROR) {
        localizedString(R.string.error_network_unavailable)
    } else if (this == UNKNOWN_ERROR) {
        localizedString(R.string.error_unexpected)
    } else {
        val jsonObject = JSONObject(this)
        val message = jsonObject.getString("message")

        if (message == EXPIRED_TOKEN_KO) {
            event?.baseEvent(BaseViewModel.Event.ExpiredToken)
            ""
        } else if (message.endsWith(".")) {
            message
        } else {
            "$message."
        }
    }
}

fun String?.parseErrorCode(event: BaseViewModel? = null): String {
    return if (this == "") {
        ""
    } else if (this == NETWORK_ERROR) {
        localizedString(R.string.error_network_unavailable)
    } else if (this == UNKNOWN_ERROR) {
        localizedString(R.string.error_unexpected)
    } else {
        val jsonObject = JSONObject(this)

        if (jsonObject.getString("message") == EXPIRED_TOKEN_KO) {
            event?.baseEvent(BaseViewModel.Event.ExpiredToken)
            ""
        } else {
            jsonObject.getString("code")
        }
    }
}

fun String.bookCodeToSettlementUrl(settlementId : Long): String {
    return "https://floney.onelink.me$appsflyer_settlement_url?settlementId=${settlementId}&bookCode=${this}"
}

fun String.toCategoryCode(): String = when (this) {
    "수입" -> "INCOME"
    "지출" -> "OUTCOME"
    "이체" -> "TRANSFER"
    else -> ""
}

fun String.toCategoryName(): String = when (this) {
    "INCOME" -> "수입"
    "OUTCOME" -> "지출"
    "TRANSFER" -> "이체"
    else -> ""
}

fun String?.formatMoneyWithCurrency(): String {
    if (this.isNullOrBlank()) return "0" + CurrencyUtil.currency

    return when {
        this.startsWith("+") || this.startsWith("-") -> {
            this.substring(1).trim() + CurrencyUtil.currency
        }
        else -> {
            this.trim() + CurrencyUtil.currency
        }
    }
}
