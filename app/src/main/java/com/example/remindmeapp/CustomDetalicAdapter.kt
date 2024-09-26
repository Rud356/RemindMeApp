package com.example.remindmeapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CustomDetalicAdapter(private val context: Context, private val items: List<String>) : ArrayAdapter<String>(context, R.layout.list_short_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_detalic_item, parent, false)

        val itemText = view.findViewById<TextView>(R.id.itemText)
        itemText.text = items[position]

        return view
    }
}
