package com.example.remindmeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.remindmeapp.custom.FragmentSwitcher
import com.example.remindmeapp.events.DbHelper

class AllEventsFragment : Fragment() {

    private lateinit var dbHelper: DbHelper
    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_all_events, container, false)
        val backView: ImageView = view.findViewById(R.id.back_view)
        val addEventBtn: Button = view.findViewById(R.id.addEventButton)

        dbHelper = DbHelper(requireContext(), null)

        // Найдите ListView
        listView = view.findViewById(R.id.listAllEvent)

        // Получите данные из базы данных
        val events = dbHelper.getAllEvents()

        // Создайте и установите адаптер
        val adapter = EventDetalicAdapter(requireContext(), events)
        listView.adapter = adapter

        backView.setOnClickListener {
            FragmentSwitcher.backPress(requireActivity())
        }

        addEventBtn.setOnClickListener {
            FragmentSwitcher.replaceFragment(AddEventFragment())
        }

        return view
    }
}
