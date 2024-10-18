package com.example.remindmeapp.registration

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

object RegistrationService {
    var user : User? = null

    private var loggedIn : Boolean = false
    private const val fileName : String = "users.json"

    fun getLoggedIn(context: Context) : Boolean {
        user = readUserFromJsonFile(context)

        // TODO: каждый раз отправлять еще и запрос на сервер, если неактуально - удалять

        if (user != null)
            loggedIn = true

        return loggedIn
    }

    fun loginUser(context: Context, username: String, password : String){
        user = User(username, password)
        loggedIn = true
        writeUserToJsonFile(context, user!!)
    }

    fun logOut(context: Context){
        loggedIn = false
        deleteFile(context)
    }

    private fun writeUserToJsonFile(context: Context, user: User) {
        val file = File(context.filesDir, fileName)

        if (!file.exists()) {
            file.createNewFile()
        }

        val jsonObject = JSONObject()
        jsonObject.put("username", user.username)
        jsonObject.put("password", user.password)

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

        return User(username, password)
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