package com.example.remindmeapp.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.example.remindmeapp.ColorPickerView
import com.example.remindmeapp.R
import com.example.remindmeapp.RepeatSelectorView
import com.example.remindmeapp.custom.DateFormatHelper
import com.example.remindmeapp.custom.DateTimeFormatHelper
import com.example.remindmeapp.custom.FragmentSwitcher
import com.example.remindmeapp.custom.TimeFormatHelper
import com.example.remindmeapp.events.DbHelper
import com.example.remindmeapp.events.Event
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.text.isEmpty

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

        val colorPicker: ColorPickerView = view.findViewById(R.id.colorPicker)
        val repeatSelector: RepeatSelectorView = view.findViewById(R.id.repeatSelector)
        val eventNameTxt: EditText = view.findViewById(R.id.editTextNameEvent)
        val eventTextTxt: EditText = view.findViewById(R.id.editTextNotification)
        dateInput = view.findViewById(R.id.editTextDate)
        timeInput = view.findViewById(R.id.timeStart)

        // Меняем дату на выбранный день, если перешли с окна дня событий
        val argDate = arguments?.getString("date")

        if (argDate != null){
            date = LocalDate.parse(argDate, DateFormatHelper.dateFormatter)
            updateDateText()
        }
        else {
            date = LocalDate.now()
        }

        time = LocalTime.now()

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
            if (eventNameTxt.text.isEmpty() || dateInput.text.isEmpty() || timeInput.text.isEmpty()) {
                Toast.makeText(requireContext(), "Необходимо заполнить все обязательные поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val name = eventNameTxt.text.toString()
                val text = eventTextTxt.text.toString()
                val color = colorPicker.getColor()
                val dateTime : LocalDateTime = LocalDateTime.of(date, time)
                val currentTime = DateTimeFormatHelper.toZoneString(LocalDateTime.now())
                val triggeredPeriod = repeatSelector.getSelectedOption()
                val isPeriodic = triggeredPeriod > 0

                // TODO: Логика добавления ивента в БД и на сервер
                val dbHelper = DbHelper(requireContext(), null)
                dbHelper.addEvent(
                    Event(0, name, text, color, currentTime, currentTime, DateTimeFormatHelper.toZoneString(dateTime), isPeriodic, triggeredPeriod)
                )

                FragmentSwitcher.backPress(requireActivity())
            } catch (e: Exception){
                println(e.stackTrace)
                Toast.makeText(requireContext(), "Некорректные данные", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }

        return view
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.DialogTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                date = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                updateDateText()
            },
            this.date.year,
            this.date.month.value - 1,
            this.date.dayOfMonth
        )

        datePickerDialog.datePicker.minDate = DateFormatHelper.toLong(LocalDate.now())
        datePickerDialog.show()
    }

    private fun updateDateText(){
        dateInput.setText(date.format(DateFormatHelper.dateFormatter))
    }

    private fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                val curDateTime = LocalDateTime.now()
                val newTime = LocalDateTime.of(date, LocalTime.of(selectedHour, selectedMinute))

                if (newTime.isBefore(curDateTime)) {
                    Toast.makeText(
                        requireContext(),
                        "Нельзя выбрать время, которое уже прошло",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@TimePickerDialog
                }

                time = LocalTime.of(selectedHour, selectedMinute)
                timeInput.setText(TimeFormatHelper.toString(time))
            },
            this.time.hour, this.time.minute, true
        )

        timePickerDialog.show()
    }
}
