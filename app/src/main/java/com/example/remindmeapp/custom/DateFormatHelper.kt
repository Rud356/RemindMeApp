package com.example.remindmeapp.custom

import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatHelper {
    private const val DATE_FORMAT = "dd MMMM yyyy"
    private const val DATE_FORMAT_DETALIC = "dd/MM/yyyy"

    val dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT, Locale("ru"))
    val dateDetalicFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_DETALIC, Locale("ru"))

    fun toString(date : LocalDate) : String {
        return date.format(dateFormatter)
    }

    fun toLong(date: LocalDate) : Long {
        return date.atStartOfDay(ZoneOffset.systemDefault()).toInstant().toEpochMilli()
    }
}