package com.example.remindmeapp.custom

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeFormatHelper {
    private const val TIME_FORMAT = "HH:mm"
    val timeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT, Locale("ru"))

    fun toString(time : LocalTime) : String {
        return time.format(timeFormatter)
    }
}