package com.example.remindmeapp.custom

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeFormatHelper {
    private const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"
    val dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT, Locale("ru"))

    fun toString(dateTime : LocalDateTime) : String {
        return dateTime.format(dateTimeFormatter)
    }

    fun parseZone(string: String) : LocalDateTime {
        val zonedDateTime = ZonedDateTime.parse(string, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val result =  zonedDateTime.withZoneSameInstant(ZoneOffset.systemDefault()).toLocalDateTime()
        println("Local time: $result from $string")
        return result
    }

    fun toZoneString(dateTime: LocalDateTime) : String {
        val offsetTime: ZonedDateTime = dateTime.atZone(ZoneOffset.systemDefault())
        println("At current zone: $offsetTime")
        val utcTime = offsetTime.withZoneSameInstant(ZoneOffset.UTC)
        println("At UTC zone: $utcTime")
        return offsetTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}