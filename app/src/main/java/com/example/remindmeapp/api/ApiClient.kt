package com.example.remindmeapp.api

import com.example.remindmeapp.events.Event
import com.example.remindmeapp.registration.User
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import okhttp3.CookieJar;

class ApiClient {
    private val _gson = Gson()
    private val _url : String

    constructor(ip: String){
        _url = "http://$ip:9090"
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // CookieJar для автоматического управления cookies
    private val cookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            println("Save for ${url.host} - $cookies")
            cookieStore[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            println("Load for ${url.host} ${cookieStore[url.host]}")
            return cookieStore[url.host] ?: emptyList()
        }
    }

    // OkHttpClient с CookieJar и логированием
    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(loggingInterceptor)
        .build()

    suspend fun loginUser(user: User): ResponseData {
        val url = "${_url}/users/login"
        return postJson(url, user)
    }

    suspend fun logoutUser(): ResponseData? {
        val loginUrl = "${_url}/users/logout"
        return postJson(loginUrl, "")
    }

    suspend fun registerUser(username: String, password: String): ResponseData {
        val url = "${_url}/users/register"
        val request = AuthRequest(username, password)
        return postJson(url, request)
    }

    suspend fun createEvent(event: Event): ResponseData {
        val url = "${_url}/reminders/"
        val request = EventRequest(event.name, event.descr, event.color, event.triggeredAt, event.isPeriodic, event.triggeredPeriod)
        return postJson(url, request)
    }

    suspend fun getEvents() : ResponseData {
        val url = "${_url}/reminders/"
        return getJson(url)
    }

    suspend fun getEvent(id : Int) : ResponseData {
        val url = "${_url}/reminders/$id"
        return getJson(url)
    }

    suspend fun deleteEvent(id : Int) : ResponseData {
        val url = "${_url}/reminders/$id"
        return deleteJson(url)
    }

    suspend fun patchEvent(event: Event) : ResponseData {
        val url = "${_url}/reminders/${event.foreignId}"
        return patchJson(url, EventRequest(event.name, event.descr, event.color, event.triggeredAt, event.isPeriodic, event.triggeredPeriod))
    }

    private suspend fun postJson(url: String, fromToBody: Any): ResponseData = withContext(Dispatchers.IO) {
        val mediaType = ("application/json; charset=utf-8").toMediaTypeOrNull()
        val body = _gson.toJson(fromToBody)
        val requestBody = body.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
                return@withContext ResponseData(response.code, response.body?.string(), response.isSuccessful)
            }
        } catch (e : Exception){
            println(e)
            return@withContext ResponseData(0, "", false)
        }
    }

    private suspend fun getJson(url: String): ResponseData = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
                return@withContext ResponseData(response.code, response.body?.string(), response.isSuccessful)
            }
        } catch (e : Exception){
            println(e.stackTrace)
            return@withContext ResponseData(0, "", false)
        }
    }

    private suspend fun deleteJson(url: String): ResponseData = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
                return@withContext ResponseData(response.code, response.body?.string(), response.isSuccessful)
            }
        } catch (e : Exception){
            println(e.stackTrace)
            return@withContext ResponseData(0, "", false)
        }
    }

    private suspend fun patchJson(url: String, fromToBody: Any): ResponseData = withContext(Dispatchers.IO) {
        val mediaType = ("application/json; charset=utf-8").toMediaTypeOrNull()
        val body = _gson.toJson(fromToBody)
        val requestBody = body.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .patch(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
                return@withContext ResponseData(response.code, response.body?.string(), response.isSuccessful)
            }
        } catch (e : Exception){
            println(e.stackTrace)
            return@withContext ResponseData(0, "", false)
        }
    }
}