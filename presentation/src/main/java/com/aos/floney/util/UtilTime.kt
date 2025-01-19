package com.aos.floney.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.math.abs


fun convertStringToDate(time: String): Date {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.parse(time) ?: Date()
}
fun getCurrentDateTimeString(): String {
    val mNow = System.currentTimeMillis()
    val mDate = Date(mNow)
    val mFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    mFormat.timeZone = TimeZone.getTimeZone("UTC")
    return mFormat.format(mDate)
}

fun getAdvertiseCheck(advertiseTime : String): Long {
    if (advertiseTime.isEmpty())
        return 0
    else
        return 359 - (convertStringToDate(getCurrentDateTimeString()).time - convertStringToDate(advertiseTime).time) / (60 * 1000)
}

fun getAdvertiseTenMinutesCheck(advertiseTime : String): Long {
    return if (advertiseTime.isEmpty())
        0
    else
        10 - (convertStringToDate(getCurrentDateTimeString()).time - convertStringToDate(advertiseTime).time) / (60 * 1000)
}

