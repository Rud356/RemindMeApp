package com.example.remindmeapp

import OnSwipeTouchListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.remindmeapp.events.Date

class DayEventsFragment : Fragment() {

    private lateinit var date : Date

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.activity_day_events, container, false)
        val currentDateTxt : TextView = view.findViewById(R.id.current_date)

        val back : ImageView = view.findViewById(R.id.back_view)
        val addEvent : Button = view.findViewById(R.id.addEventButton)

        date = arguments?.getParcelable("date")!!

        back.setOnClickListener {
            FragmentSwitcher.backPress(requireActivity())
        }

        addEvent.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("date", date)

            val addEventFragment = AddEventFragment()
            addEventFragment.arguments = bundle
            FragmentSwitcher.replaceFragment(addEventFragment)
        }

        view.findViewById<View>(R.id.day_events).setOnTouchListener(object : OnSwipeTouchListener(requireContext()) {
            override fun onSwipeBottom() {
                FragmentSwitcher.replaceFragmentWithAnim(MainFragment(), R.anim.slide_in_top, R.anim.slide_out_bottom)
            }
        })

        currentDateTxt.text = date.toString()

        return view
    }
}