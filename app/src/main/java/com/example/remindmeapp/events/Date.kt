package com.example.remindmeapp.events

import android.os.Parcel
import android.os.Parcelable

data class Date (val day : Int, val month : Int, val year : Int) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    private fun monthToString(month: Int): String {
        return when (month) {
            1 -> "января"
            2 -> "февраля"
            3 -> "марта"
            4 -> "апреля"
            5 -> "мая"
            6 -> "июня"
            7 -> "июля"
            8 -> "августа"
            9 -> "сентября"
            10 -> "октября"
            11 -> "ноября"
            12 -> "декабря"
            else -> "неизвестный месяц"
        }
    }

    override fun toString(): String {
        return "$day ${monthToString(month)} $year"
    }

    fun toStringDigits() : String {
        return "$day.$month.$year"
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, p1: Int) {
        parcel.writeInt(day)
        parcel.writeInt(month)
        parcel.writeInt(year)
    }

    companion object CREATOR : Parcelable.Creator<Date> {
        override fun createFromParcel(parcel: Parcel): Date {
            return Date(parcel)
        }

        override fun newArray(size: Int): Array<Date?> {
            return arrayOfNulls(size)
        }
    }
}