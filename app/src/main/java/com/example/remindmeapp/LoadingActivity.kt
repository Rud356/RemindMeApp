package com.example.remindmeapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoadingActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_loading_window)

        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loading_window)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/

        // Тут проверяем залогинились мы или нет
        var isLogged = true
        val intent : Intent

        if (isLogged) {
            intent = Intent(this, LoginActivity::class.java)
        }
        else {
            intent = Intent(this, MainActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}