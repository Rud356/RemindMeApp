package com.example.remindmeapp.fragments

import OnSwipeTouchListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.remindmeapp.EventShortAdapter
import com.example.remindmeapp.R
import com.example.remindmeapp.custom.DateFormatHelper
import com.example.remindmeapp.custom.FragmentSwitcher
import com.example.remindmeapp.events.DbHelper
import java.time.LocalDate

class MainFragment : Fragment() {
    private lateinit var dbHelper: DbHelper
    private lateinit var listView : ListView

    private var date : LocalDate = LocalDate.now();
    private lateinit var textSelectedDate : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_main, container, false)
        val calendarView : CalendarView = view.findViewById(R.id.calendarView)
        listView = view.findViewById(R.id.listView)
        textSelectedDate = view.findViewById(R.id.textSelectedDate)

        dbHelper = DbHelper(requireContext(), null)
        updateSelectedDate()
        calendarView.setDate(DateFormatHelper.toLong(date));
        calendarView.minDate = DateFormatHelper.toLong(LocalDate.now());

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            date = LocalDate.of(year, month + 1, dayOfMonth)
            updateSelectedDate()
        }

        view.findViewById<View>(R.id.main).setOnTouchListener(object : OnSwipeTouchListener(requireContext()) {
            override fun onSwipeTop() {
                FragmentSwitcher.replaceFragmentWithAnim(createDayEvents(), R.anim.slide_in_bottom, R.anim.slide_out_top)
            }
        })

        return view
    }

    private fun createDayEvents() : DayEventsFragment {
        val bundle = Bundle()
        bundle.putString("date", date.format(DateFormatHelper.dateFormatter))

        val dayEventsFragment = FragmentSwitcher.DayEventsFragment
        dayEventsFragment.arguments = bundle
        return dayEventsFragment
    }

    private fun updateSelectedDate(){
        val currentDate = LocalDate.now()

        if (currentDate == date)
            textSelectedDate.text = "Сегодня"
        else
            textSelectedDate.text = DateFormatHelper.toString(date)

        val events = dbHelper.getEventsByDayAll(date, 4)
        val adapter = EventShortAdapter(requireContext(), events)
        listView.adapter = adapter
    }
}
