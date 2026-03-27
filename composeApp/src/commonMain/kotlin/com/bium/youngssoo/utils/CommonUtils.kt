package com.bium.youngssoo.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

//
//@Composable
//fun pxToDp(pxValue: Float): Dp {
//    val density = LocalDensity.current.density
//    return (pxValue / density).dp
//}

@Composable
fun pxToDp(pxValue: Float): Dp {
    return with(LocalDensity.current) {
        pxValue.toDp()
    }
}

fun stringTimeToLong(time: String): Long {
    println("time : $time")
    try {
        // 만약 LocalDateTime으로 쓰고 싶으면:
        val localDateTime = LocalDateTime.parse(time)
        val epochMillisFromLocal = localDateTime.toInstant(TimeZone.UTC).toEpochMilliseconds()
        println(epochMillisFromLocal) // 1758986208000

        // UTC 기준 epoch milli로 변환
        val epochMilli = localDateTime.toInstant(TimeZone.UTC).toEpochMilliseconds()
        return epochMilli
    } catch (e: Exception) {
        e.printStackTrace()
        return 0
    }
}

fun formatWithComma(number: Int): String {
    val s = number.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in s.length - 1 downTo 0) {
        sb.append(s[i])
        count++
        if (count % 3 == 0 && i != 0) {
            sb.append(',')
        }
    }
    return sb.reverse().toString()
}

fun formatTimeAgoFromLong(timestampMillis: Long): String {
    val now = Clock.System.now().toEpochMilliseconds()
    val diffMillis = now - timestampMillis
    val duration = diffMillis.milliseconds

    val days = duration.toInt(DurationUnit.DAYS)
    return if (days >= 1) {
        "${days}일 전"
    } else {
        val hours = duration.toInt(DurationUnit.HOURS)
        "${hours}시간 전"
    }
}

fun timeAgoFromIsoString(isoString: String, timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    println("timeAgoFromIsoString : $isoString" )
    val now = Clock.System.now()
    val thenInstant = runCatching { Instant.parse(isoString) }.getOrElse {
        // 오프셋 없는 경우 → 로컬 시간으로 해석
        LocalDateTime.parse(isoString).toInstant(timeZone)
    }

    val future = thenInstant > now
    val diffSeconds = abs(thenInstant.epochSeconds - now.epochSeconds)

    val minutes = diffSeconds / 60
    val hours = diffSeconds / 3600
    val days = diffSeconds / (3600 * 24)
    val months = days / 30
    val years = days / 365

    val suffix = if (future) "후" else "전"

    return when {
        diffSeconds < 60 -> "${diffSeconds}초$suffix"
        minutes < 60 -> "${minutes}분$suffix"
        hours < 24 -> "${hours}시간$suffix"
        days < 30 -> "${days}일$suffix"
        months < 12 -> "${months}달$suffix"
        else -> "${years}년$suffix"
    }
}