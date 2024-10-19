package com.example.remindmeapp.events

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(val context: Context, val factory : SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "events.db", factory, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE events (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, descr TEXT, color TEXT, " +
                "createdAt DATE, editedAt DATE, triggeredAt DATE, isPeriodic BOOLEAN, triggeredPeriod INTEGER, isActive BOOLEAN)"

        db!!.execSQL(query)
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        p1: Int,
        p2: Int,
    ) {
        db!!.execSQL("DROP TABLE IF EXISTS events")
        onCreate(db)
    }

    fun addEvent(event : Event){
        val values = ContentValues()
        values.put("name", event.name)
        values.put("descr", event.descr)
        values.put("color", event.color)
        values.put("createdAt", event.createdAt.toString())
        values.put("editedAt", event.editedAt.toString())
        values.put("triggeredAt", event.triggeredAt.toString())
        values.put("isPeriodic", event.isPeriodic)
        values.put("triggeredPeriod", event.triggerPeriod)
        values.put("isActive", event.isActive)

        val db = this.writableDatabase
        db.insert("events", null, values)
        db.close()
    }
}