package com.example.remindmeapp

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.remindmeapp.custom.DateFormatHelper
import com.example.remindmeapp.custom.DateTimeFormatHelper
import com.example.remindmeapp.custom.TimeFormatHelper
import com.example.remindmeapp.events.Event
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class EventShortAdapter(private val context: Context, private val events: List<Event>) : ArrayAdapter<Event>(context, 0, events) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val event = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_short_item, parent, false)

        // Найдите элементы списка и заполните их данными
        val itemText = view.findViewById<TextView>(R.id.itemText)
        val itemTime = view.findViewById<TextView>(R.id.itemTime)
        val iconRepeat = view.findViewById<ImageView>(R.id.iconSecond)

        if (event == null)
            return view

        val triggeredAt = LocalDateTime.parse(event.triggeredAt)

        itemText.text = event.name
        itemTime.text = triggeredAt.format(TimeFormatHelper.timeFormatter)
        iconRepeat.visibility = if (event.isPeriodic == true){
            View.VISIBLE
        } else{
            View.GONE
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
