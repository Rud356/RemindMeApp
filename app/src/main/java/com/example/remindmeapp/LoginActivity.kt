package com.example.remindmeapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.remindmeapp.registration.RegistrationService

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

            // TODO: Сделать проверку удалось ли войти отправив запрос на сервак

            if (logined) {
                val intent = Intent(this, MainActivity::class.java)
                RegistrationService.loginUser(this, login, pass)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Не удалось авторизоваться, попробуйте снова", Toast.LENGTH_LONG).show()
            }
        }

        registerButton.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}