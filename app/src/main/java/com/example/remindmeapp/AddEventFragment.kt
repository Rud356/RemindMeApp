package com.example.remindmeapp

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.remindmeapp.events.Date
import com.example.remindmeapp.events.DateTime
import com.example.remindmeapp.events.DbHelper
import com.example.remindmeapp.events.Event
import com.example.remindmeapp.events.Time
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AddEventFragment : Fragment() {

    private var date : Date? = null
    private lateinit var dateInput : EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_add_event, container, false)

        val backView: ImageView = view.findViewById(R.id.imageButtonBack)
        val saveEventBtn: Button = view.findViewById(R.id.buttonSave)

        val eventNameTxt: EditText = view.findViewById(R.id.editTextNameEvent)
        val eventTextTxt: EditText = view.findViewById(R.id.editTextNotification)
        dateInput = view.findViewById(R.id.editTextDate)
        val eventStartTxt: EditText = view.findViewById(R.id.timeStart)
        val colorPicker: ColorPickerView = view.findViewById(R.id.color_picker)

        // Меняем дату на текущий день, если перешли с окна дня событий
        date = arguments?.getParcelable("date")

        if (date != null){
            dateInput.setText(date?.toStringDigits())
        }

        dateInput.setOnClickListener {
            showDatePickerDialog()
        }

        backView.setOnClickListener {
            FragmentSwitcher.backPress(requireActivity())
        }

        saveEventBtn.setOnClickListener {
            // TODO: Логика добавления ивента в БД и на сервер
            if (eventNameTxt.text.isEmpty() || date == null || eventStartTxt.text.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Необходимо заполнить все обязательные поля",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            try {
                var formatter = DateTimeFormatter.ofPattern("HH:mm")
                val time = LocalTime.parse(eventStartTxt.text, formatter)

                val name = eventNameTxt.text.toString()
                val text = eventTextTxt.text.toString()
                val color = colorPicker.getColor()
                val dateTime : DateTime = DateTime(date!!, Time(time.hour, time.minute))
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

                val currentTime = LocalDateTime.now().format(formatter)
                val dbHelper = DbHelper(requireContext(), null)
                dbHelper.addEvent(Event(0, name, text, color, currentTime, currentTime, LocalDateTime.parse(dateTime.toString(), formatter).format(formatter), false, 0, true))

                FragmentSwitcher.backPress(requireActivity())
            } catch (e: Exception){
                Toast.makeText(requireContext(), "Некорректный формат времени", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }

        return view
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            date = Date(
                selectedDay,
                selectedMonth + 1,
                selectedYear
            ) // Добавляем 1 к месяцу, т.к. он начинается с 0
            dateInput.setText(date?.toStringDigits())
        }, year, month, day)

        datePickerDialog.show()
    }
}
