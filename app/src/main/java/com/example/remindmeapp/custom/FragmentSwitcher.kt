package com.example.remindmeapp.custom

import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.remindmeapp.fragments.AddEventFragment
import com.example.remindmeapp.fragments.AllEventsFragment
import com.example.remindmeapp.fragments.DayEventsFragment
import com.example.remindmeapp.fragments.EditEventFragment
import com.example.remindmeapp.fragments.MainFragment
import java.lang.IllegalStateException

object FragmentSwitcher {
    private lateinit var fragmentManager: FragmentManager
    private var containerId: Int = 0

    val MainFragment : MainFragment = MainFragment()
    val AddEventFragment : AddEventFragment = AddEventFragment()
    val AllEventsFragment : AllEventsFragment = AllEventsFragment()
    val DayEventsFragment : DayEventsFragment = DayEventsFragment()
    val EditEventFragment : EditEventFragment = EditEventFragment()

    private val subscribers = mutableListOf<() -> Unit>()

    fun onEventTriggered(action: () -> Unit) = subscribers.add(action)

    fun initialize(fragmentManager: FragmentManager, containerId: Int) {
        this.fragmentManager = fragmentManager
        this.containerId = containerId
    }

    fun updateEvents(){
        subscribers.forEach { it() }
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
