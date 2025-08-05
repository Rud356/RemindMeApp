package com.example.remindmeapp.remind

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import com.example.remindmeapp.api.ApiClient
import com.example.remindmeapp.events.DbHelper

class ReminderApplication : Application() {
    companion object {
        const val channelName = "Reminders"
        const val changelDescr = "Channel for reminders"
        const val channelId = "reminders"

        lateinit var alarmHelper : AlarmHelper
        lateinit var apiClient: ApiClient
    }

    override fun onCreate() {
        super.onCreate()

        alarmHelper = AlarmHelper(getSystemService(ALARM_SERVICE) as AlarmManager, this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
                .apply {
                    description = changelDescr
                    setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
                }

            val notificationManager : NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}