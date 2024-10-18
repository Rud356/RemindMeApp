package com.example.remindmeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment

class AllEventsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_all_events, container, false)
        val backView: ImageView = view.findViewById(R.id.back_view)
        val addEventBtn: Button = view.findViewById(R.id.addEventButton)

        backView.setOnClickListener {
            FragmentSwitcher.backPress(requireActivity())
        }

        addEventBtn.setOnClickListener {
            FragmentSwitcher.replaceFragment(AddEventFragment())
        }

        return view
    }
}
