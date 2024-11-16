package com.example.remindmeapp.events

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.remindmeapp.api.CreateResponse
import com.example.remindmeapp.api.EventResponse
import com.example.remindmeapp.custom.DateTimeFormatHelper
import com.example.remindmeapp.custom.FragmentSwitcher
import com.example.remindmeapp.remind.ReminderApplication
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

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
        const val COLUMN_FOREIGN_ID = "foreignId"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_NAME TEXT, $COLUMN_DESCR TEXT, $COLUMN_COLOR TEXT, " +
                "$COLUMN_CREATED_AT DATE, $COLUMN_EDITED_AT DATE, $COLUMN_TRIGGERED_AT DATE, $COLUMN_IS_PERIODIC BOOLEAN, $COLUMN_TRIGGERED_PERIOD INTEGER, $COLUMN_FOREIGN_ID INTEGER)"

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

    fun updateFromExternal(){
        CoroutineScope(Dispatchers.Main).launch{
            val res = ReminderApplication.apiClient.getEvents()

            if (res.isSuccessful){
                val forSyncEvents = getEvents("SELECT * FROM $TABLE_NAME WHERE $COLUMN_FOREIGN_ID is NULL")
                val externalEvents : List<EventResponse> = Gson().fromJson(res.body, object : TypeToken<List<EventResponse>>() {}.type)

                var event : Event?
                for (eventResp in externalEvents){
                    event = getEventByForeignId(eventResp.id)

                    if (event != null){
                        val editedTime = DateTimeFormatHelper.parseZone(event.editedAt)
                        val resEditedTime = DateTimeFormatHelper.parseZone(eventResp.last_edited_at)

                        println(resEditedTime.isBefore(editedTime))
                        if (resEditedTime.isBefore(editedTime)) {
                            // Обновление ивента на сервере
                            ReminderApplication.apiClient.patchEvent(event)
                        }
                        else {
                            // Обновление ивента в локальной БД
                            event.name = eventResp.title
                            event.descr = eventResp.description
                            event.isPeriodic = eventResp.is_periodic
                            event.triggeredPeriod = eventResp.trigger_period
                            event.triggeredAt = eventResp.triggered_at
                            event.editedAt = eventResp.last_edited_at
                            event.color  = eventResp.color_code

                            if (isExpiredAndNeedDelete(event)){
                                deleteEventServer(event)
                            }
                            else {
                                updateEventTrigger(event)
                            }
                        }
                    }
                    else {
                        // Создание ивента в локальной БД
                        addEvent(Event(0, eventResp.title, eventResp.description, eventResp.color_code, eventResp.created_at,
                            eventResp.last_edited_at, eventResp.triggered_at, eventResp.is_periodic, eventResp.trigger_period, eventResp.id))
                    }
                }

                for (event in forSyncEvents){
                    addExistedEventServer(event)
                }
            }
            else {
                println("Не удалось обновить события из внешней БД")
            }

            FragmentSwitcher.updateEvents()
        }
    }

    fun deleteAllEvents() {
        val events = getAllEvents()

        for (event in events)
            deleteEventById(event.id)
    }

    fun updateAllEvents() {
        val events = getEvents("SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_TRIGGERED_AT ASC")

        for (event in events) {
            if (isExpiredAndNeedDelete(event))
            {
                println("Event with ${event.id} deleted.")
                deleteEventById(event.id)
            } else {
                val event = updateEventTrigger(event)
            }
        }

        FragmentSwitcher.updateEvents()
    }

    fun isExpiredAndNeedDelete(event: Event) : Boolean {
        val triggeredAt = DateTimeFormatHelper.parseZone(event.triggeredAt)
        val currentDateTime = LocalDateTime.now()

        if (triggeredAt.isBefore(currentDateTime)) {
            if (!event.isPeriodic)
                return true
        }

        return false
    }

    fun updateEventTrigger(event: Event){
        val triggeredAt = DateTimeFormatHelper.parseZone(event.triggeredAt)
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
                event.triggeredAt = DateTimeFormatHelper.toZoneString(newTriggeredAt)
                updateEvent(event)
            }
        }
    }

    fun addExistedEventServer(event : Event){
        CoroutineScope(Dispatchers.Main).launch {
            val res = ReminderApplication.apiClient.createEvent(event)

            if (res.code == 200){
                val json = res.body
                val resp : CreateResponse = Gson().fromJson(json, CreateResponse::class.java)

                if (resp.is_created)
                    event.foreignId = resp.event_id
            }

            updateEvent(event)
        }
    }

    fun addEventServer(event : Event){
        CoroutineScope(Dispatchers.Main).launch {
            val res = ReminderApplication.apiClient.createEvent(event)

            if (res.code == 200){
                val json = res.body
                val resp : CreateResponse = Gson().fromJson(json, CreateResponse::class.java)

                if (resp.is_created)
                    event.foreignId = resp.event_id
            }

            addEvent(event)
        }
    }

    fun addEvent(event : Event){
        val db = this@DbHelper.writableDatabase
        val newId = db.insert(TABLE_NAME, null, getContentValues(event))
        db.close()

        if (newId != -1L) {
            event.id = newId.toInt()
            ReminderApplication.alarmHelper.addEvent(event)
        }
    }

    fun updateEventServer(event: Event) {
        CoroutineScope(Dispatchers.Main).launch {
            ReminderApplication.apiClient.patchEvent(event)
        }
    }

    fun updateEvent(event: Event): Int {
        val eventToRemove = getEventById(event.id)

        if (eventToRemove == null)
            return -1

        ReminderApplication.alarmHelper.removeEvent(eventToRemove)

        val db = this.writableDatabase
        // Выполняем обновление записи по id
        val result = db.update(TABLE_NAME, getContentValues(event), "$COLUMN_ID = ?", arrayOf(event.id.toString()))
        db.close()

        ReminderApplication.alarmHelper.addEvent(event)

        return result
    }

    private fun getContentValues(event: Event) : ContentValues {
        return ContentValues().apply {
            put(COLUMN_NAME, event.name)
            put(COLUMN_DESCR, event.descr)
            put(COLUMN_COLOR, event.color)
            put(COLUMN_CREATED_AT, event.createdAt.toString())
            put(COLUMN_EDITED_AT, event.editedAt.toString())
            put(COLUMN_TRIGGERED_AT, event.triggeredAt.toString())
            put(COLUMN_IS_PERIODIC, event.isPeriodic)
            put(COLUMN_TRIGGERED_PERIOD, event.triggeredPeriod)
            put(COLUMN_FOREIGN_ID, event.foreignId)
        }
    }

    fun deleteEventServer(event: Event) {
        CoroutineScope(Dispatchers.Main).launch {
            if (event.foreignId != null)
                ReminderApplication.apiClient.deleteEvent(event.foreignId!!)
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
        var events = getEvents("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = $id")

        if (events.isNotEmpty())
            return events[0]

        return null
    }

    fun getEventByForeignId(id: Int): Event? {
        var events = getEvents("SELECT * FROM $TABLE_NAME WHERE $COLUMN_FOREIGN_ID = $id")

        if (events.isNotEmpty())
            return events[0]

        return null
    }

    fun getAllEvents(): List<Event> {
        return getEvents("SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_TRIGGERED_AT ASC")
    }

    fun getEventsByDay(date: LocalDate, count: Int): List<Event> {
        val dateString = date.toString()
        var query = """
                    SELECT *, DATE($COLUMN_TRIGGERED_AT, 'utc', 'localtime') as TriggerTime FROM $TABLE_NAME
                    WHERE (
                        -- Обычные (непериодические) события на указанный день
                        (DATE($COLUMN_TRIGGERED_AT, 'utc', 'localtime') = '$dateString')
                        OR 
                        -- Периодические события, которые должны сработать в указанный день
                        ($COLUMN_IS_PERIODIC = 1 AND
                         DATE(TriggerTime, '+' || (CAST((julianday('$dateString') - julianday(TriggerTime)) / $COLUMN_TRIGGERED_PERIOD as INTEGER) * $COLUMN_TRIGGERED_PERIOD) || ' days') = '$dateString'
                        )
                    )
                    ORDER BY $COLUMN_TRIGGERED_AT ASC
                """.trimIndent()

        if (count > 0)
            query += " LIMIT $count"

        return getEvents(query)
    }

    fun getEvents(query : String) : List<Event> {
        var events = mutableListOf<Event>()
        var event: Event? = null

        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                event = Event(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    descr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCR)),
                    color = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                    editedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EDITED_AT)),
                    triggeredAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIGGERED_AT)),
                    isPeriodic = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PERIODIC)) > 0,
                    triggeredPeriod = cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                            COLUMN_TRIGGERED_PERIOD
                        )
                    ),
                    foreignId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOREIGN_ID))
                )

                events.add(event)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return events
    }
}