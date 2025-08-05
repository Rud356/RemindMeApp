package com.example.remindmeapp.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.remindmeapp.R
import com.example.remindmeapp.registration.RegistrationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.jvm.java

class LoadingActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContentView(R.layout.activity_loading_window)

        // Тут проверяем залогинились мы или нет
        CoroutineScope(Dispatchers.Main).launch{
            var isLogged = RegistrationService.getLoggedIn(this@LoadingActivity)
            val intent : Intent

            if (!isLogged) {
                intent = Intent(this@LoadingActivity, LoginActivity::class.java)
            }
            else {
                // TODO: обновить ивенты
                intent = Intent(this@LoadingActivity, MainActivity::class.java)
            }

            startActivity(intent)
            finish()
        }
    }
}