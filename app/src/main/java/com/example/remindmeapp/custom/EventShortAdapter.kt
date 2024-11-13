package com.example.remindmeapp

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.remindmeapp.custom.FragmentSwitcher
import com.example.remindmeapp.custom.TimeFormatHelper
import com.example.remindmeapp.events.Event
import com.example.remindmeapp.fragments.EditEventFragment
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

class EventShortAdapter(private val context: Context, private val events: List<Event>) : ArrayAdapter<Event>(context, 0, events) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val event = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_short_item, parent, false)

        // Найдите элементы списка и заполните их данными
        val itemText = view.findViewById<TextView>(R.id.itemText)
        val itemTime = view.findViewById<TextView>(R.id.itemTime)
        val iconRepeat = view.findViewById<ImageView>(R.id.iconSecond)
        val textRepeat = view.findViewById<TextView>(R.id.periodTime)

        if (event == null)
            return view

        view.findViewById<LinearLayout>(R.id.background).setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("eventId", event.id)

            val editEventFragment = FragmentSwitcher.EditEventFragment
            editEventFragment.arguments = bundle
            FragmentSwitcher.replaceFragment(editEventFragment)
        }

        val triggeredAt = OffsetDateTime.parse(event.triggeredAt).atZoneSameInstant(ZoneId.systemDefault())

        itemText.text = event.name
        itemTime.text = triggeredAt.format(TimeFormatHelper.timeFormatter)

        if (event.isPeriodic == true){
            iconRepeat.visibility = View.VISIBLE
            textRepeat.setText(event.triggeredPeriod.toString())
        } else{
            iconRepeat.visibility = View.GONE
            textRepeat.setText("")
        }

        val linearLayout: LinearLayout = view.findViewById(R.id.background)
        val background = linearLayout.background as LayerDrawable
        val colorLayer = background.getDrawable(0) // 0 — это индекс первого слоя
        val newColor = Color.parseColor(event.color)

        if (colorLayer is GradientDrawable) {
            colorLayer.setColor(newColor)
        } else {
            Log.e("DrawableError", "Не найден элемент для смены цвета")
        }

        return view
    }
}
