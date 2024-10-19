package com.example.remindmeapp.custom

import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import java.lang.IllegalStateException
import kotlin.isInitialized

object FragmentSwitcher {
    private lateinit var fragmentManager: FragmentManager
    private var containerId: Int = 0

    fun initialize(fragmentManager: FragmentManager, containerId: Int) {
        this.fragmentManager = fragmentManager
        this.containerId = containerId
    }

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        if (!::fragmentManager.isInitialized) {
            throw IllegalStateException("FragmentManager is not initialized. Call initialize() first.")
        }

        val transaction = fragmentManager.beginTransaction()
            .replace(containerId, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    fun replaceFragmentWithAnim(fragment: Fragment, animEnter : Int, animExit : Int, addToBackStack: Boolean = true){
        if (!::fragmentManager.isInitialized) {
            throw IllegalStateException("FragmentManager is not initialized. Call initialize() first.")
        }

        val transaction = fragmentManager.beginTransaction()
            .setCustomAnimations(animEnter, animExit)
            .replace(containerId, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    fun backPress(fragmentActivity: FragmentActivity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Используем onBackPressedDispatcher для Android 13 и выше
            fragmentActivity.onBackPressedDispatcher.onBackPressed()
        } else {
            fragmentActivity.onBackPressed()
        }
    }
}
