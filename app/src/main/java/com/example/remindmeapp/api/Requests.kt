package com.example.remindmeapp.api

data class AuthRequest(val username: String, val password: String)
data class EventRequest(val title: String, val description: String, val color_code : String, val triggered_at : String, val is_periodic : Boolean, val trigger_period : Int)