package com.example.remindmeapp.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.remindmeapp.R
import com.example.remindmeapp.api.ApiClient
import com.example.remindmeapp.registration.RegistrationService
import com.example.remindmeapp.registration.User
import com.example.remindmeapp.remind.ReminderApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.jvm.java
import kotlin.text.trim

class LoginActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login_window)

        val loginField = findViewById<EditText>(R.id.editTextLogin)
        val ipField = findViewById<EditText>(R.id.editTextIp)
        val passwordField = findViewById<EditText>(R.id.editTextPassword)

        val loginButton = findViewById<Button>(R.id.buttonLogin);
        val registerButton = findViewById<TextView>(R.id.textViewRegister);

        loginButton.setOnClickListener{
            val login = loginField.text.toString().trim()
            val ip = ipField.text.toString().trim()
            val pass = passwordField.text.toString().trim()

            var logined = true

            if (login == "" || ip == "" || pass == ""){
                Toast.makeText(this, "Необходимо заполнить все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener;
            }

            CoroutineScope(Dispatchers.Main).launch {
                val res = RegistrationService.loginUser(this@LoginActivity, User(login, pass, ip))

                if (!res.isSuccessful)
                    logined = false

                if (logined) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Не удалось авторизоваться, попробуйте снова", Toast.LENGTH_LONG).show()
                }
            }
        }

        registerButton.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}