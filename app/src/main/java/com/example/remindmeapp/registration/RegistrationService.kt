package com.example.remindmeapp.registration

import android.content.Context
import com.example.remindmeapp.api.ApiClient
import com.example.remindmeapp.api.ResponseData
import com.example.remindmeapp.events.DbHelper
import com.example.remindmeapp.remind.ReminderApplication
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

object RegistrationService {
    var user : User? = null

    private var loggedIn : Boolean = false
    private const val fileName : String = "users.json"

    suspend fun getLoggedIn(context: Context) : Boolean {
        user = readUserFromJsonFile(context)

        if (user != null) {
            // TODO: добавить проверку если нет сети или ресурс недоступен, но мы уже были залогинены
            loginUser(context, user!!)
        }

        return loggedIn
    }

    suspend fun loginUser(context: Context, user: User) : ResponseData {
        ReminderApplication.apiClient = ApiClient(user.ip)
        val res = ReminderApplication.apiClient.loginUser(user)

        if (res.isSuccessful) {
            loggedIn = true
            writeUserToJsonFile(context, user)

            val dbHelper = DbHelper(context, null)
            dbHelper.updateAllEvents()
            dbHelper.updateFromExternal()
        }

        return res
    }

    suspend fun registerUser(context: Context, username: String, password : String, ip : String) : ResponseData {
        user = User(username, password, ip)
        ReminderApplication.apiClient = ApiClient(user!!.ip)
        val res = ReminderApplication.apiClient.registerUser(user!!.username, user!!.password)

        if (res.isSuccessful) {
            // Если зарегестрировались, то можно и войти попытаться
            return loginUser(context, user!!)
        }

        return res
    }

    suspend fun logOut(context: Context){
        ReminderApplication.apiClient.logoutUser()
        loggedIn = false
        deleteFile(context)

        val dbHelper = DbHelper(context, null)
        val db = dbHelper.writableDatabase
        db.delete("events", null, null)
        db.close()
    }

    private fun writeUserToJsonFile(context: Context, user: User) {
        val file = File(context.filesDir, fileName)

        if (!file.exists()) {
            file.createNewFile()
        }

        val jsonObject = JSONObject()
        jsonObject.put("username", user.username)
        jsonObject.put("password", user.password)
        jsonObject.put("ip", user.ip)

        FileOutputStream(file).use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                writer.write(jsonObject.toString())
            }
        }
    }

    private fun readUserFromJsonFile(context: Context): User? {
        val file = File(context.filesDir, fileName)

        if (!file.exists()) {
            return null
        }

        val jsonString = file.readText()

        val jsonObject = JSONObject(jsonString)
        val username = jsonObject.getString("username")
        val password = jsonObject.getString("password")
        val ip = jsonObject.getString("ip")

        return User(username, password, ip)
    }

    private fun deleteFile(context: Context) {
        val file = File(context.filesDir, fileName)

        if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                println("Файл успешно удален")
            } else {
                println("Не удалось удалить файл")
            }
        } else {
            println("Файл не существует")
        }
    }
}