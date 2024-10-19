package com.example.remindmeapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.remindmeapp.registration.RegistrationService

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
            // TODO: Сделать проверку удалось ли войти
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

            // TODO: Необходимо отправить запрос на сервак

            if (registered) {
                val intent = Intent(this, MainActivity::class.java)
                RegistrationService.loginUser(this, login, pass)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Не удалось зарегистрироваться, попробуйте снова", Toast.LENGTH_LONG).show()
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