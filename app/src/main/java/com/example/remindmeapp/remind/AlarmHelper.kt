package com.example.remindmeapp.remind

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import com.example.remindmeapp.events.Event
import java.time.OffsetDateTime
import java.time.ZoneId

class AlarmHelper {

    val alarmManager: AlarmManager
    val context: Context

    constructor(alarmManager: AlarmManager, context: Context){
        this.alarmManager = alarmManager
        this.context = context
    }

    fun addEvent(event: Event){
        val triggerAtTime = OffsetDateTime.parse(event.triggeredAt).toLocalDateTime()
        val triggerInMillis = triggerAtTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Вычисляем время напоминания за час до события
        val reminderHourInMillis = triggerInMillis - 60 * 60 * 1000  // 1 час в миллисекундах
        val currentTimeMillis = System.currentTimeMillis()

        val pendingIntent = Utils.getPendingIntent(context, event.id, event.name, event.descr)
        val pendingIntentBeforeHour = Utils.getPendingIntent(context, event.id * -1, "Через час: ${event.name}", "Напоминание за час до события")

        println("Pending id ${event.id} for event ${event.name}")

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()){
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerInMillis, pendingIntent)

            if (reminderHourInMillis > currentTimeMillis) {
                println("Pending id ${event.id * -1} for event ${event.name} before 1 hour")
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderHourInMillis,
                    pendingIntentBeforeHour
                )
            }
        }
    }

    fun removeEvent(event : Event){
        val pendingIntent = Utils.getPendingIntentForCancel(context, event.id, event.name, event.descr)
        val pendingIntentBeforeHour = Utils.getPendingIntentForCancel(context, event.id * -1, "Через час: ${event.name}", "Напоминание за час до события")

        if (pendingIntent != null) {
            println("Pending id ${event.id} for event ${event.name} canceled")
            alarmManager.cancel(pendingIntent)
        }

        if (pendingIntentBeforeHour != null) {
            alarmManager.cancel(pendingIntentBeforeHour)
            println("Pending id ${event.id * -1} for event ${event.name} before 1 hour canceled")
        }
    }
}