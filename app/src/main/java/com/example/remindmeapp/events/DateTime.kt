package com.example.remindmeapp.events

data class DateTime(val date : Date, val time : Time) {
    override fun toString(): String {
        return "${date.year}-${date.month}-${date.day} ${time.hour.toString().padStart(2, '0')}:${time.minutes.toString().padStart(2, '0')}"
    }
}
