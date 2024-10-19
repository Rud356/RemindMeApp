package com.example.remindmeapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.remindmeapp.custom.DateFormatHelper
import com.example.remindmeapp.custom.FragmentSwitcher
import com.example.remindmeapp.custom.TimeFormatHelper
import com.example.remindmeapp.events.DbHelper
import com.example.remindmeapp.events.Event
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AddEventFragment : Fragment() {

    private lateinit var date : LocalDate
    private lateinit var time : LocalTime

    private lateinit var dateInput : EditText
    private lateinit var timeInput : EditText

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
        timeInput = view.findViewById(R.id.timeStart)
        val colorPicker: ColorPickerView = view.findViewById(R.id.colorPicker)

        // Меняем дату на текущий день, если перешли с окна дня событий
        val argDate = arguments?.getString("date")

        if (argDate != null){
            date = LocalDate.parse(argDate, DateFormatHelper.dateFormatter)
            updateDateText()
        }

        dateInput.setOnClickListener {
            showDatePickerDialog()
        }

        timeInput.setOnClickListener {
            showTimePickerDialog()
        }

        backView.setOnClickListener {
            FragmentSwitcher.backPress(requireActivity())
        }

        saveEventBtn.setOnClickListener {
            // TODO: Логика добавления ивента в БД и на сервер
            if (eventNameTxt.text.isEmpty() || dateInput.text.isEmpty() || timeInput.text.isEmpty()) {
                Toast.makeText(requireContext(), "Необходимо заполнить все обязательные поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val name = eventNameTxt.text.toString()
                val text = eventTextTxt.text.toString()
                val color = colorPicker.getColor()
                val dateTime : LocalDateTime = LocalDateTime.of(date, time)
                val currentTime = LocalDateTime.now().toString()
                val dbHelper = DbHelper(requireContext(), null)
                dbHelper.addEvent(Event(0, name, text, color, currentTime, currentTime, dateTime.toString(), false, 0, true))

                FragmentSwitcher.backPress(requireActivity())
            } catch (e: Exception){
                Toast.makeText(requireContext(), "Некорректные данные", Toast.LENGTH_SHORT).show()
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
            date = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
            updateDateText()
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun updateDateText(){
        dateInput.setText(date.format(DateFormatHelper.dateFormatter))
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                time = LocalTime.of(selectedHour, selectedMinute)
                timeInput.setText(TimeFormatHelper.toString(time))
            },
            hour, minute, true
        )

        timePickerDialog.show()
    }
}
