package com.example.remindmeapp.remind

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class Utils {
    companion object {
        fun getPendingIntent(context: Context, id : Int, text : String, descr : String) : PendingIntent {
            val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                putExtra("id", id)
                putExtra("text", text)
                putExtra("descr", descr)
            }

            return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        fun getPendingIntentForCancel(context: Context, id : Int, text : String, descr : String) : PendingIntent? {
            val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                putExtra("id", id)
                putExtra("text", text)
                putExtra("descr", descr)
            }

            return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        }
    }
}