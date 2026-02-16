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
            title = localizeAlarmTitle(context, alarm.title),
            body = localizeAlarmBody(context, alarm.body),
            imgUrl = alarm.imgUrl,
            date = timeAgo, // 이제 'date'는 경과한 시간으로 표시됩니다.
            received = alarm.received
        )
    }
}

private fun localizeAlarmTitle(context: Context, rawTitle: String): String {
    return when (rawTitle.trim()) {
        "플로니", "Floney" -> context.getString(R.string.notification_title)
        else -> rawTitle
    }
}

private fun localizeAlarmBody(context: Context, rawBody: String): String {
    val body = rawBody.trim()

    // Settle ledger
    Regex("^(.+?) 가계부를 정산해보세요\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_settle_ledger, it.groupValues[1])
    }
    if (body == "가계부를 정산해보세요.") {
        return context.getString(R.string.notification_settle_ledger_generic)
    }
    Regex("^Settle the (.+?) team ledger\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_settle_ledger, it.groupValues[1])
    }
    if (body == "Settle the team ledger.") {
        return context.getString(R.string.notification_settle_ledger_generic)
    }

    // Budget / spending
    Regex("^예산의\\s*([0-9]+(?:\\.[0-9]+)?)%를 사용했어요\\. 남은 예산을 확인해보세요\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_budget_usage, it.groupValues[1])
    }
    Regex("^You[’']ve used\\s*([0-9]+(?:\\.[0-9]+)?)% of your budget\\. Check what you can still spend\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_budget_usage, it.groupValues[1])
    }
    if (body == "오늘의 지출을 추가하고 사용 내역을 확인해보세요." || body == "Add today's spending and review your usage.") {
        return context.getString(R.string.notification_add_spending)
    }

    // Added record
    Regex("^(.+?)님이 내역을 추가했어요\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_user_added_record, it.groupValues[1])
    }
    Regex("^(.+?) added a record\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_user_added_record, it.groupValues[1])
    }

    // Joined ledger
    Regex("^(.+?)님이 (.+?) 가계부에 들어왔어요\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_user_joined, it.groupValues[1], it.groupValues[2])
    }
    Regex("^(.+?)님이 가계부에 들어왔어요\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_user_joined_generic, it.groupValues[1])
    }
    Regex("^(.+?) joined the (.+?) team ledger\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_user_joined, it.groupValues[1], it.groupValues[2])
    }
    Regex("^(.+?) joined the team ledger\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_user_joined_generic, it.groupValues[1])
    }

    // Left ledger
    Regex("^(.+?)님이 (.+?) 가계부를 나갔습니다\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_user_left, it.groupValues[1], it.groupValues[2])
    }
    Regex("^(.+?)님이 가계부를 나갔습니다\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_user_left_generic, it.groupValues[1])
    }
    Regex("^(.+?) left the (.+?) team ledger\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_user_left, it.groupValues[1], it.groupValues[2])
    }
    Regex("^(.+?) left the team ledger\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_user_left_generic, it.groupValues[1])
    }

    // Currency changed
    Regex("^(.+?) 가계부의 화폐가 (.+?)로 변경되었어요\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_currency_changed, it.groupValues[1], it.groupValues[2])
    }
    Regex("^화폐가 (.+?)\\(으\\)로 변경되었어요\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_currency_changed_generic, it.groupValues[1])
    }
    Regex("^Currency in the (.+?) team ledger was changed to (.+?)\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_currency_changed, it.groupValues[1], it.groupValues[2])
    }
    Regex("^Currency was changed to (.+?)\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_currency_changed_generic, it.groupValues[1])
    }

    // Ledger reset
    Regex("^(.+?) 가계부가 초기화 되었어요\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_ledger_reset, it.groupValues[1])
    }
    if (body == "가계부가 초기화 되었어요.") {
        return context.getString(R.string.notification_ledger_reset_generic)
    }
    Regex("^The (.+?) team ledger was reset\\.$").matchEntire(body)?.let {
        return context.getString(R.string.notification_ledger_reset, it.groupValues[1])
    }
    if (body == "The team ledger was reset.") {
        return context.getString(R.string.notification_ledger_reset_generic)
    }

    return rawBody
}
