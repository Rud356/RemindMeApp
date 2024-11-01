package com.example.remindmeapp.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import com.example.remindmeapp.R
import com.example.remindmeapp.custom.FragmentSwitcher
import com.example.remindmeapp.registration.RegistrationService
import com.example.remindmeapp.remind.ReminderApplication
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        isGranted: Boolean -> if (!isGranted){
            Toast.makeText(this, "Необходимо дать разрешение на отправку уведомлений для корректной работы приложения", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driwer)
        FragmentSwitcher.initialize(supportFragmentManager, R.id.fragment_container)


        // Права на отправку уведомлений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !ReminderApplication.alarmHelper.alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }


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
                R.id.nav_calendar -> {
                    FragmentSwitcher.replaceFragment(FragmentSwitcher.MainFragment)
                }
                R.id.nav_events -> {
                    FragmentSwitcher.replaceFragment(FragmentSwitcher.AllEventsFragment)
                }
                R.id.nav_exit -> {
                    RegistrationService.logOut(this)
                    intent = Intent(this, LoadingActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_close -> {
                    finish()
                }
            }

            drawerLayout.closeDrawers()
            true
        }

        if (savedInstanceState == null) {
            FragmentSwitcher.replaceFragment(FragmentSwitcher.MainFragment, false)
        }
    }

    @Override
    override fun onBackPressed() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}