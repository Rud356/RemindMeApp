package com.example.remindmeapp.events

data class Event(val id : Int, var name : String, var descr : String, var color : String, val createdAt : String, var editedAt : String,
                 var triggeredAt : String, var isPeriodic : Boolean, var triggeredPeriod : Int, var isActive : Boolean)