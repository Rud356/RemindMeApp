package com.example.remindmeapp.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.remindmeapp.R
import com.example.remindmeapp.registration.RegistrationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.jvm.java
import kotlin.text.trim

class RegisterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register_window)

        val loginField = findViewById<EditText>(R.id.editTextLogin)
        val ipField = findViewById<EditText>(R.id.editTextIp)
        val passField = findViewById<EditText>(R.id.editTextPassword)
        val passConfirmField = findViewById<EditText>(R.id.editTextConfirmPassword)

        val registerButton = findViewById<Button>(R.id.buttonRegister);
        val loginButton = findViewById<TextView>(R.id.textViewLogin);
        val backButton = findViewById<ImageButton>(R.id.imageButtonBack);

        registerButton.setOnClickListener{
            val login = loginField.text.toString().trim()
            val ip = ipField.text.toString().trim()
            val pass = passField.text.toString().trim()
            val passConfirm = passConfirmField.text.toString().trim()

            var registered = true

            if (login == "" || ip == "" || pass == "" || passConfirm == ""){
                Toast.makeText(this, "Необходимо заполнить все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener;
            }

            if (pass != passConfirm) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                return@setOnClickListener;
            }

            CoroutineScope(Dispatchers.Main).launch {
                val res = RegistrationService.registerUser(this@RegisterActivity, login, pass, ip)

                if (!res.isSuccessful){
                    registered = false
                    println(res.code)
                    when (res.code){
                        400 -> Toast.makeText(this@RegisterActivity, "Логин и пароль должны быть не менее 8 символов.", Toast.LENGTH_LONG).show()
                        409 -> Toast.makeText(this@RegisterActivity, "Пользователь с таким именем уже зарегистрирован.", Toast.LENGTH_LONG).show()
                        else -> Toast.makeText(this@RegisterActivity, "Не удалось зарегистрироваться, попробуйте снова", Toast.LENGTH_LONG).show()
                    }
                }

                if (registered) {
                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        loginButton.setOnClickListener{
            goLogin()
        }

        backButton.setOnClickListener{
            goLogin()
        }
    }

    fun goLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}