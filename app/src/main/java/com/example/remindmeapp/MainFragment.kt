package com.example.remindmeapp

import OnSwipeTouchListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.fragment.app.Fragment
import com.example.remindmeapp.events.Date
import java.util.Calendar

class MainFragment : Fragment() {

    private var date : Date = getCurrentDate();

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_main, container, false)
        val calendarView : CalendarView = view.findViewById(R.id.calendarView)

        calendarView.setOnDateChangeListener { cal, year, month, dayOfMonth ->
            date = Date(dayOfMonth, month, year)
        }

        view.findViewById<View>(R.id.main).setOnTouchListener(object : OnSwipeTouchListener(requireContext()) {
            override fun onSwipeTop() {
                val bundle = Bundle()
                bundle.putParcelable("date", date)

                val dayEventsFragment = DayEventsFragment()
                dayEventsFragment.arguments = bundle

                FragmentSwitcher.replaceFragmentWithAnim(dayEventsFragment, R.anim.slide_in_bottom, R.anim.slide_out_top)
            }
        })

        return view
    }

    private fun getCurrentDate(): Date {
        val calendar = Calendar.getInstance()
        return Date(
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1,  // Месяц от 0 до 11, поэтому добавляем 1
            calendar.get(Calendar.YEAR)
        )
    }
}
