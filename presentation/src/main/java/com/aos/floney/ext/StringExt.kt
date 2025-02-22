package com.aos.floney.ext

import android.provider.Settings.Global.getString
import com.aos.floney.BuildConfig.appsflyer_settlement_url
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import org.json.JSONObject
import timber.log.Timber

fun String?.parseErrorMsg(event: BaseViewModel? = null): String {
    return if(this == "") {
        ""
    } else if(this == "NetworkError") {
        "네트워크에 연결되어 있지 않거나 원활하지 않습니다."
    } else if(this == "unKnownError") {
        "예상 못한 에러 발생! 플로니팀에 제보 해주세요."
    } else {
        val jsonObject = JSONObject(this)

        if(jsonObject.getString("message").equals("만료된 토큰입니다")) {
            event?.baseEvent(BaseViewModel.Event.ExpiredToken)
            ""
        } else {
            val msg = jsonObject.getString("message")
            if(msg.substring(msg.length - 1, msg.length) == ".") {
                msg
            } else {
                "$msg."
            }
        }
    }
}

fun String?.parseErrorCode(event: BaseViewModel? = null): String {
    return if(this == "") {
        ""
    } else if(this == "NetworkError") {
        "네트워크에 연결되어 있지 않거나 원활하지 않습니다."
    } else if(this == "unKnownError") {
        "예상 못한 에러 발생! 플로니팀에 제보 해주세요."
    } else {
        val jsonObject = JSONObject(this)

        if(jsonObject.getString("message").equals("만료된 토큰입니다")) {
            event?.baseEvent(BaseViewModel.Event.ExpiredToken)
            ""
        } else {
            val msg = jsonObject.getString("code")
            msg
        }
    }
}

fun String.bookCodeToSettlementUrl(settlementId : Long): String {
    return "https://floney.onelink.me$appsflyer_settlement_url?settlementId=${settlementId}&bookCode=${this}"
}