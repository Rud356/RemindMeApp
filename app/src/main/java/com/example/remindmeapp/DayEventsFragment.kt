package com.example.remindmeapp

import OnSwipeTouchListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.remindmeapp.custom.DateFormatHelper
import com.example.remindmeapp.custom.FragmentSwitcher
import com.example.remindmeapp.events.DbHelper
import java.time.LocalDate

class DayEventsFragment : Fragment() {
    private lateinit var dbHelper: DbHelper
    private lateinit var listView : ListView

    private lateinit var date : LocalDate

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.activity_day_events, container, false)
        val currentDateTxt : TextView = view.findViewById(R.id.current_date)

        val back : ImageView = view.findViewById(R.id.back_view)
        val addEvent : Button = view.findViewById(R.id.addEventButton)

        listView = view.findViewById(R.id.listDayEvents)
        dbHelper = DbHelper(requireContext(), null)

        val argDate = arguments?.getString("date")

        if (argDate == null)
        {
            FragmentSwitcher.backPress(requireActivity())
            return view
        }

        date = LocalDate.parse(argDate, DateFormatHelper.dateFormatter)

        val events = dbHelper.getEventsByDayAll(date, -1)
        val adapter = EventShortAdapter(requireContext(), events)
        listView.adapter = adapter

        if (date == LocalDate.now())
            currentDateTxt.text = "Сегодня"
        else
            currentDateTxt.text = date.format(DateFormatHelper.dateFormatter)

        back.setOnClickListener {
            FragmentSwitcher.backPress(requireActivity())
        }

        addEvent.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("date", date.format(DateFormatHelper.dateFormatter))

            val addEventFragment = AddEventFragment()
            addEventFragment.arguments = bundle
            FragmentSwitcher.replaceFragment(addEventFragment)
        }

        view.findViewById<View>(R.id.day_events).setOnTouchListener(object : OnSwipeTouchListener(requireContext()) {
            override fun onSwipeBottom() {
                FragmentSwitcher.replaceFragmentWithAnim(MainFragment(), R.anim.slide_in_top, R.anim.slide_out_bottom)
            }
        })


        return view
    }
}