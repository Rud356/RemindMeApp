package com.example.remindmeapp.api

data class CreateResponse(val is_created : Boolean, val event_id: Int)
data class EventResponse(val id: Int, val title: String, val description: String, val color_code: String, val created_at: String, val last_edited_at: String, val triggered_at: String, val is_active: Boolean, val is_periodic: Boolean, val trigger_period: Int)

data class ResponseData(val code : Int, val body : String?, val isSuccessful : Boolean)