package com.example.remindmeapp.custom

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeFormatHelper {
    private const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"
    val dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT, Locale("ru"))

    fun toString(dateTime : LocalDateTime) : String {
        return dateTime.format(dateTimeFormatter)
    }
}