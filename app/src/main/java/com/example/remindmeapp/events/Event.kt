package com.example.remindmeapp.events

data class Event(val id : Int, val name : String, val descr : String, val color : String, val createdAt : String, val editedAt : String,
                 val triggeredAt : String, val isPeriodic : Boolean, val triggerPeriod : Int, val isActive : Boolean)