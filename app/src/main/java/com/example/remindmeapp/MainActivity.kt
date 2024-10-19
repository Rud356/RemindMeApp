package com.example.remindmeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.remindmeapp.custom.FragmentSwitcher
import com.example.remindmeapp.registration.RegistrationService

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driwer)
        FragmentSwitcher.initialize(supportFragmentManager, R.id.fragment_container)

        // Настроить Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Подключение DrawerLayout и NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        // Настроить ActionBarDrawerToggle
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Слушатель нажатий на элементы меню
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_user -> {
                    Toast.makeText(this, "Профиль выбран ${RegistrationService.user?.username}", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_events -> {
                    FragmentSwitcher.replaceFragment(AllEventsFragment())
                }
                R.id.nav_exit -> {
                    // TODO: Выход из профиля
                    RegistrationService.logOut(this)
                    intent = Intent(this, LoadingActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            drawerLayout.closeDrawers()
            true
        }

        // Загружаем основной контент из activity_main.xml
        if (savedInstanceState == null) {
            FragmentSwitcher.replaceFragment(MainFragment(), false)
        }
    }
}