package com.example.remindmeapp.remind

import com.example.remindmeapp.R

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.remindmeapp.custom.FragmentSwitcher
import com.example.remindmeapp.events.DbHelper

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null){
            val id = intent?.getIntExtra("id", 0)

            val text = intent?.getStringExtra("text") ?: "Пустой заголовок"
            val descr = intent?.getStringExtra("descr") ?: ""

            val notificationManager = NotificationManagerCompat.from(context)
            val builder = NotificationCompat.Builder(context, ReminderApplication.channelId)
                .setSmallIcon(R.drawable.ic_clover)
                .setContentTitle(text)
                .setContentText(descr)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                return
            }

            notificationManager.notify(id!!, builder.build())

            val dbHelper = DbHelper(context, null)
            val event = dbHelper.getEventById(id)

            if (event != null) {
                if (dbHelper.isExpiredAndNeedDelete(event)){
                    dbHelper.deleteEventServer(event)
                    dbHelper.deleteEventById(event.id)
                }
                dbHelper.updateEventTrigger(event)
            }

            println("Событие сработало и обновлено")
            FragmentSwitcher.updateEvents()
        }
    }
}