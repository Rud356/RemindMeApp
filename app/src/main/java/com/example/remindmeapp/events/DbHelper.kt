package com.example.remindmeapp.events

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.remindmeapp.remind.ReminderApplication
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class DbHelper(val context: Context, val factory : SQLiteDatabase.CursorFactory?) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "events.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "events"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_DESCR = "descr"
        const val COLUMN_COLOR = "color"
        const val COLUMN_CREATED_AT = "createdAt"
        const val COLUMN_EDITED_AT = "editedAt"
        const val COLUMN_TRIGGERED_AT = "triggeredAt"
        const val COLUMN_IS_PERIODIC = "isPeriodic"
        const val COLUMN_TRIGGERED_PERIOD = "triggeredPeriod"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_NAME TEXT, $COLUMN_DESCR TEXT, $COLUMN_COLOR TEXT, " +
                "$COLUMN_CREATED_AT DATE, $COLUMN_EDITED_AT DATE, $COLUMN_TRIGGERED_AT DATE, $COLUMN_IS_PERIODIC BOOLEAN, $COLUMN_TRIGGERED_PERIOD INTEGER)"

        db!!.execSQL(query)
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        p1: Int,
        p2: Int,
    ) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addEvent(event : Event){
        val values = ContentValues()
        values.put(COLUMN_NAME, event.name)
        values.put(COLUMN_DESCR, event.descr)
        values.put(COLUMN_COLOR, event.color)
        values.put(
            COLUMN_CREATED_AT,
            LocalDateTime.parse(event.createdAt).atOffset(ZoneOffset.UTC).toString()
        )
        values.put(COLUMN_EDITED_AT,
            LocalDateTime.parse(event.editedAt).atOffset(ZoneOffset.UTC).toString()
        )
        values.put(
            COLUMN_TRIGGERED_AT,
            LocalDateTime.parse(event.triggeredAt).atOffset(ZoneOffset.UTC).toString()
        )
        values.put(COLUMN_IS_PERIODIC, event.isPeriodic)
        values.put(COLUMN_TRIGGERED_PERIOD, event.triggeredPeriod)

        val db = this.writableDatabase
        val newId = db.insert(TABLE_NAME, null, values)
        db.close()

        if (newId != -1L) {
            event.id = newId.toInt()
            ReminderApplication.alarmHelper.addEvent(event)
        }
    }

    fun deleteEventById(id: Int): Int {
        val eventToRemove = getEventById(id)

        if (eventToRemove != null)
            ReminderApplication.alarmHelper.removeEvent(eventToRemove)

        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    fun getEventById(id: Int): Event? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            """
                SELECT
                $COLUMN_ID, $COLUMN_NAME, $COLUMN_DESCR, $COLUMN_COLOR,
                strftime('%Y-%m-%dT%H:%M:%S', DATETIME($COLUMN_CREATED_AT, 'localtime')) AS $COLUMN_CREATED_AT,
                strftime('%Y-%m-%dT%H:%M:%S', DATETIME($COLUMN_EDITED_AT, 'localtime')) AS $COLUMN_EDITED_AT,
                strftime('%Y-%m-%dT%H:%M:%S', DATETIME(julianday($COLUMN_TRIGGERED_AT) + COALESCE(NextTriggerStep, 0), 'localtime')) AS $COLUMN_TRIGGERED_AT,
                $COLUMN_IS_PERIODIC,
                $COLUMN_TRIGGERED_PERIOD,
                COALESCE(CAST(round(NextTriggerStep - DateDiff, 0) AS INTEGER), 0) AS NextEventInDays,
                datetime($COLUMN_TRIGGERED_AT, 'localtime') as RealTrigger
            FROM (
                SELECT *,
                CASE
                    WHEN ABS(DateDiff) <= 1 THEN
                      CAST(ABS(DateDiff) AS INTEGER)
                    ELSE
                      CAST((DateDiff / $COLUMN_TRIGGERED_PERIOD) + 1 AS INTEGER) * $COLUMN_TRIGGERED_PERIOD
                  END AS NextTriggerStep
                FROM (
                    SELECT
                    *,
                    julianday(DATETIME('now', 'localtime')) - julianday($COLUMN_TRIGGERED_AT, 'localtime') AS DateDiff
                    FROM events
                ) WHERE NOT ($COLUMN_IS_PERIODIC = 0 AND DATETIME($COLUMN_TRIGGERED_AT, 'localtime') < DATETIME('now', 'localtime'))
            )
            WHERE $COLUMN_ID = ?
            """.trimIndent(),
            arrayOf(id.toString())
        )

        var event: Event? = null
        if (cursor.moveToFirst()) {
            event = Event(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                descr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCR)),
                color = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR)),
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                editedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EDITED_AT)),
                triggeredAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIGGERED_AT)),
                isPeriodic = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PERIODIC)) > 0,
                triggeredPeriod = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRIGGERED_PERIOD)),
            )
        }

        cursor.close()
        db.close()
        return event
    }

    fun updateEvent(event: Event): Int {
        val eventToRemove = getEventById(event.id)

        if (eventToRemove == null)
            return -1

        ReminderApplication.alarmHelper.removeEvent(eventToRemove)

        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, event.name)
            put(COLUMN_DESCR, event.descr)
            put(COLUMN_COLOR, event.color)
            put(
                COLUMN_CREATED_AT,
                LocalDateTime.parse(event.createdAt).atOffset(ZoneOffset.UTC).toString()
            )
            put(COLUMN_EDITED_AT,
                LocalDateTime.parse(event.editedAt).atOffset(ZoneOffset.UTC).toString()
            )
            put(
                COLUMN_TRIGGERED_AT,
                LocalDateTime.parse(event.triggeredAt).atOffset(ZoneOffset.UTC).toString()
            )
            put(COLUMN_IS_PERIODIC, event.isPeriodic)
            put(COLUMN_TRIGGERED_PERIOD, event.triggeredPeriod)
        }

        // Выполняем обновление записи по id
        val result = db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(event.id.toString()))
        db.close()

        ReminderApplication.alarmHelper.addEvent(event)

        return result
    }

    fun updateAllEvent() {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT " +
                    "$COLUMN_ID, $COLUMN_NAME, $COLUMN_DESCR," +
                    "$COLUMN_COLOR, " +
                    "strftime('%Y-%m-%dT%H:%M:%S', DATETIME($COLUMN_CREATED_AT, 'localtime')) AS $COLUMN_CREATED_AT, " +
                    "strftime('%Y-%m-%dT%H:%M:%S', DATETIME($COLUMN_EDITED_AT, 'localtime')) AS $COLUMN_EDITED_AT, " +
                    "strftime('%Y-%m-%dT%H:%M:%S', DATETIME($COLUMN_TRIGGERED_AT, 'localtime')) AS $COLUMN_TRIGGERED_AT, " +
                    "$COLUMN_IS_PERIODIC, $COLUMN_TRIGGERED_PERIOD" +
                    " FROM $TABLE_NAME ORDER BY $COLUMN_TRIGGERED_AT ASC",
            null)

        if (cursor.moveToFirst()) {
            do {
                val event = Event(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    descr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCR)),
                    color = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                    editedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EDITED_AT)),
                    triggeredAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIGGERED_AT)),
                    isPeriodic = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PERIODIC)) > 0,
                    triggeredPeriod = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRIGGERED_PERIOD)),
                )

                updateEventTrigger(event)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
    }

    private fun updateEventTrigger(event: Event) : Event? {
        val triggeredAt = LocalDateTime.parse(event.triggeredAt)
        val currentDateTime = LocalDateTime.now()

        if (triggeredAt.isBefore(currentDateTime)) {
            if (event.isPeriodic) {
                // Актуализация времени для периодических событий
                var newTriggeredAt = triggeredAt
                while (newTriggeredAt.isBefore(currentDateTime)) {
                    newTriggeredAt = when (event.triggeredPeriod) {
                        1 -> newTriggeredAt.plusDays(1)
                        7 -> newTriggeredAt.plusWeeks(1)
                        30 -> newTriggeredAt.plusMonths(1)
                        365 -> newTriggeredAt.plusYears(1)
                        else -> triggeredAt
                    }
                }

                println("Old triggered time = ${triggeredAt}, new triggered time = $newTriggeredAt updated.")
                event.triggeredAt = newTriggeredAt.toString()
                event.editedAt = LocalDateTime.now().toString()
                updateEvent(event)
            } else {
                println("Event with ${event.id} deleted.")
                deleteEventById(event.id)
                return null
            }
        }

        return event
    }

    fun getAllEvents(): List<Event> {
        val db = this.readableDatabase
        var query = """
            SELECT
                $COLUMN_ID, $COLUMN_NAME, $COLUMN_DESCR, $COLUMN_COLOR,
                strftime('%Y-%m-%dT%H:%M:%S', DATETIME($COLUMN_CREATED_AT, 'localtime')) AS $COLUMN_CREATED_AT,
                strftime('%Y-%m-%dT%H:%M:%S', DATETIME($COLUMN_EDITED_AT, 'localtime')) AS $COLUMN_EDITED_AT,
                strftime('%Y-%m-%dT%H:%M:%S', DATETIME(julianday($COLUMN_TRIGGERED_AT) + COALESCE(NextTriggerStep, 0), 'localtime')) AS $COLUMN_TRIGGERED_AT,
                $COLUMN_IS_PERIODIC,
                $COLUMN_TRIGGERED_PERIOD,
                COALESCE(CAST(round(NextTriggerStep - DateDiff, 0) AS INTEGER), 0) AS NextEventInDays,
                datetime($COLUMN_TRIGGERED_AT, 'localtime') as RealTrigger
            FROM (
                SELECT *,
                CASE
                    WHEN ABS(DateDiff) <= 1 THEN
                      CAST(ABS(DateDiff) AS INTEGER)
                    ELSE
                      CAST((DateDiff / $COLUMN_TRIGGERED_PERIOD) + 1 AS INTEGER) * $COLUMN_TRIGGERED_PERIOD
                  END AS NextTriggerStep
                FROM (
                    SELECT
                    *,
                    julianday(DATETIME('now', 'localtime')) - julianday($COLUMN_TRIGGERED_AT, 'localtime') AS DateDiff
                    FROM events
                ) WHERE NOT ($COLUMN_IS_PERIODIC = 0 AND DATETIME($COLUMN_TRIGGERED_AT, 'localtime') < DATETIME('now', 'localtime'))
            )
            ORDER BY RealTrigger ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        val events = mutableListOf<Event>()

        if (cursor.moveToFirst()) {
            do {
                val event = Event(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    descr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCR)),
                    color = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                    editedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EDITED_AT)),
                    triggeredAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIGGERED_AT)),
                    isPeriodic = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PERIODIC)) > 0,
                    triggeredPeriod = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRIGGERED_PERIOD)),
                )
                var tr = cursor.getString(10);
                try {
                    var updatedEvent = updateEventTrigger(event)
                    if (updatedEvent != null)
                        events.add(updatedEvent)
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return events
    }

    fun getEventsByDayAll(date: LocalDate, count : Int) : List<Event> {
        val db = this.readableDatabase
        // Форматируем дату в строку формата "yyyy-MM-dd" для сравнения только дня
        val dateString = date.toString()
        var query = """
            SELECT
                $COLUMN_ID, $COLUMN_NAME, $COLUMN_DESCR, $COLUMN_COLOR,
                strftime('%Y-%m-%dT%H:%M:%S', DATETIME($COLUMN_CREATED_AT, 'localtime')) AS $COLUMN_CREATED_AT,
                strftime('%Y-%m-%dT%H:%M:%S', DATETIME($COLUMN_EDITED_AT, 'localtime')) AS $COLUMN_EDITED_AT,
                strftime('%Y-%m-%dT%H:%M:%S', DATETIME(julianday($COLUMN_TRIGGERED_AT) + COALESCE(NextTriggerStep, 0), 'localtime')) AS $COLUMN_TRIGGERED_AT,
                $COLUMN_IS_PERIODIC,
                $COLUMN_TRIGGERED_PERIOD,
                COALESCE(CAST(round(NextTriggerStep - DateDiff, 0) AS INTEGER), 0) AS NextEventInDays,
                datetime($COLUMN_TRIGGERED_AT, 'localtime') as RealTrigger
            FROM (
                SELECT *,
                CASE
                    WHEN ABS(DateDiff) <= 1 THEN
                      CAST(ABS(DateDiff) AS INTEGER)
                    ELSE
                      CAST((DateDiff / $COLUMN_TRIGGERED_PERIOD) + 1 AS INTEGER) * $COLUMN_TRIGGERED_PERIOD
                  END AS NextTriggerStep
                FROM (
                    SELECT
                    *,
                    julianday(DATETIME(?)) - julianday($COLUMN_TRIGGERED_AT, 'localtime') AS DateDiff
                    FROM events
                ) WHERE NOT ($COLUMN_IS_PERIODIC = 0 AND DATETIME($COLUMN_TRIGGERED_AT, 'localtime') < DATETIME(?))
            )
            ORDER BY RealTrigger ASC
        """.trimIndent()

        // Добавляем LIMIT только если count >= 0
        val selectionArgs: Array<String> = if (count >= 0) {
            query += " LIMIT ?"
            arrayOf(dateString, dateString, count.toString())
        } else {
            arrayOf(dateString, dateString)
        }
        val cursor = db.rawQuery(query, selectionArgs)
        val events = mutableListOf<Event>()

        if (cursor.moveToFirst()) {
            do {
                val event = Event(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    descr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCR)),
                    color = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                    editedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EDITED_AT)),
                    triggeredAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIGGERED_AT)),
                    isPeriodic = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PERIODIC)) > 0,
                    triggeredPeriod = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRIGGERED_PERIOD)),
                )
                var tr = cursor.getString(10);
                var updatedEvent = updateEventTrigger(event)
                if (updatedEvent != null)
                    events.add(updatedEvent)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return events
    }
}