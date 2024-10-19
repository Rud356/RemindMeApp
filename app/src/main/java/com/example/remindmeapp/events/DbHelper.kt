package com.example.remindmeapp.events

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDate
import java.time.LocalDateTime

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
        values.put("triggeredPeriod", event.triggeredPeriod)
        values.put("isActive", event.isActive)

        val db = this.writableDatabase
        db.insert("events", null, values)
        db.close()
    }

    fun getAllEvents(): List<Event> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM events ORDER BY triggeredAt ASC", null)

        val events = mutableListOf<Event>()

        if (cursor.moveToFirst()) {
            do {
                val event = Event(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    descr = cursor.getString(cursor.getColumnIndexOrThrow("descr")),
                    color = cursor.getString(cursor.getColumnIndexOrThrow("color")),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow("createdAt")),
                    editedAt = cursor.getString(cursor.getColumnIndexOrThrow("editedAt")),
                    triggeredAt = cursor.getString(cursor.getColumnIndexOrThrow("triggeredAt")),
                    isPeriodic = cursor.getInt(cursor.getColumnIndexOrThrow("isPeriodic")) > 0,
                    triggeredPeriod = cursor.getInt(cursor.getColumnIndexOrThrow("triggeredPeriod")),
                    isActive = cursor.getInt(cursor.getColumnIndexOrThrow("isActive")) > 0,
                )
                events.add(event)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return events
    }

    fun getEventsByDayAll(date: LocalDate, count : Int) : List<Event> {
        val db = this.readableDatabase
        // Форматируем дату в строку формата "yyyy-MM-dd" для сравнения только дня
        val dateString = date.toString()

        var query = "SELECT * FROM events WHERE DATE(triggeredAt) = ? ORDER BY triggeredAt ASC"

        // Добавляем LIMIT только если count >= 0
        val selectionArgs: Array<String> = if (count >= 0) {
            query += " LIMIT ?"
            arrayOf(dateString, count.toString())
        } else {
            arrayOf(dateString)
        }
        val cursor = db.rawQuery(query, selectionArgs)

        val events = mutableListOf<Event>()

        if (cursor.moveToFirst()) {
            do {
                val event = Event(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    descr = cursor.getString(cursor.getColumnIndexOrThrow("descr")),
                    color = cursor.getString(cursor.getColumnIndexOrThrow("color")),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow("createdAt")),
                    editedAt = cursor.getString(cursor.getColumnIndexOrThrow("editedAt")),
                    triggeredAt = cursor.getString(cursor.getColumnIndexOrThrow("triggeredAt")),
                    isPeriodic = cursor.getInt(cursor.getColumnIndexOrThrow("isPeriodic")) > 0,
                    triggeredPeriod = cursor.getInt(cursor.getColumnIndexOrThrow("triggeredPeriod")),
                    isActive = cursor.getInt(cursor.getColumnIndexOrThrow("isActive")) > 0
                )
                events.add(event)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return events
    }
}