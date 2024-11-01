package com.example.remindmeapp.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.remindmeapp.R
import com.example.remindmeapp.registration.RegistrationService
import kotlin.jvm.java

class LoadingActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_loading_window)

        // Тут проверяем залогинились мы или нет
        var isLogged = RegistrationService.getLoggedIn(this)
        val intent : Intent

        if (!isLogged) {
            intent = Intent(this, LoginActivity::class.java)
        }
        else {
            intent = Intent(this, MainActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}