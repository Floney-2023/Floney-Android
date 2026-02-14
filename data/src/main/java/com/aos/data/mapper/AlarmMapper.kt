package com.aos.data.mapper

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.aos.data.R
import com.aos.data.entity.response.alarm.GetAlarmEntity
import com.aos.data.entity.response.book.GetBookRepeatEntity
import com.aos.data.entity.response.home.GetCheckUserBookEntity
import com.aos.model.alarm.UiAlarmGetModel
import com.aos.model.book.UiBookRepeatModel
import com.aos.model.home.GetCheckUserBookModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.O)
fun List<GetAlarmEntity>.toUiAlarmGetEntity(context: Context): List<UiAlarmGetModel> {
    val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul")) // 현재 시간을 한국 시간대로 설정
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    return this.map { alarm ->
        val alarmTime = LocalDateTime.parse(alarm.date, formatter)
            .atZone(ZoneId.of("UTC")) // alarm.date가 UTC라고 가정
            .withZoneSameInstant(ZoneId.of("Asia/Seoul")) // 한국 시간대로 변환
        val secondsDiff = ChronoUnit.SECONDS.between(alarmTime, now).coerceAtLeast(0)
        val timeAgo = when {
            secondsDiff <= 0 -> context.getString(R.string.alarm_time_just_now)
            secondsDiff < 60 -> context.resources.getQuantityString(
                R.plurals.alarm_time_seconds_ago,
                secondsDiff.toInt(),
                secondsDiff.toInt(),
            )
            secondsDiff < 3600 -> {
                val minutes = (secondsDiff / 60).toInt()
                context.resources.getQuantityString(R.plurals.alarm_time_minutes_ago, minutes, minutes)
            }
            secondsDiff < 86_400 -> {
                val hours = (secondsDiff / 3_600).toInt()
                context.resources.getQuantityString(R.plurals.alarm_time_hours_ago, hours, hours)
            }
            secondsDiff < 604_800 -> {
                val days = (secondsDiff / 86_400).toInt()
                context.resources.getQuantityString(R.plurals.alarm_time_days_ago, days, days)
            }
            secondsDiff < 1_209_600 -> {
                val weeks = (secondsDiff / 604_800).toInt()
                context.resources.getQuantityString(R.plurals.alarm_time_weeks_ago, weeks, weeks)
            }
            else -> {
                val pattern = context.getString(R.string.alarm_old_date_pattern)
                SimpleDateFormat(pattern, Locale.getDefault()).format(Date.from(alarmTime.toInstant()))
            }
        }

        UiAlarmGetModel(
            id = alarm.id,
            title = alarm.title,
            body = alarm.body,
            imgUrl = alarm.imgUrl,
            date = timeAgo, // 이제 'date'는 경과한 시간으로 표시됩니다.
            received = alarm.received
        )
    }
}
