package com.example.remindmeapp

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
import com.example.remindmeapp.custom.DateFormatHelper
import com.example.remindmeapp.custom.FragmentSwitcher
import com.example.remindmeapp.custom.TimeFormatHelper
import com.example.remindmeapp.events.DbHelper
import com.example.remindmeapp.events.Event
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class EditEventFragment : Fragment() {

    private lateinit var date : LocalDate
    private lateinit var time : LocalTime

    private lateinit var event : Event

    private lateinit var dateInput : EditText
    private lateinit var timeInput : EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_edit_event, container, false)

        val backView: ImageView = view.findViewById(R.id.imageButtonBack)
        val deleteEventBtn: Button = view.findViewById(R.id.buttonDelete)
        val editEventBtn: Button = view.findViewById(R.id.buttonEdit)

        val colorPicker: ColorPickerView = view.findViewById(R.id.colorPicker)
        val repeatSelector: RepeatSelectorView = view.findViewById(R.id.repeatSelector)
        val eventNameTxt: EditText = view.findViewById(R.id.editTextNameEvent)
        val eventTextTxt: EditText = view.findViewById(R.id.editTextNotification)
        dateInput = view.findViewById(R.id.editTextDate)
        timeInput = view.findViewById(R.id.timeStart)

        val dbHelper = DbHelper(requireContext(), null)
        val argEventId = arguments?.getInt("eventId")

        if (argEventId == null)
            FragmentSwitcher.backPress(requireActivity())

        val argEvent = dbHelper.getEventById(argEventId!!)
        if (argEvent == null) {
            FragmentSwitcher.backPress(requireActivity())
        }

        event = argEvent!!
        // TODO: Написать сеттер для повтора
        colorPicker.setColor(event.color)
        repeatSelector.setOption(event.triggeredPeriod)
        eventNameTxt.setText(event.name);
        eventTextTxt.setText(event.descr);
        date = LocalDateTime.parse(event.triggeredAt).toLocalDate()
        time = LocalDateTime.parse(event.triggeredAt).toLocalTime()

        updateDateText()
        updateTimeText()

        dateInput.setOnClickListener {
            showDatePickerDialog()
        }

        timeInput.setOnClickListener {
            showTimePickerDialog()
        }

        backView.setOnClickListener {
            FragmentSwitcher.backPress(requireActivity())
        }

        deleteEventBtn.setOnClickListener {
            dbHelper.deleteEventById(event.id)
            FragmentSwitcher.backPress(requireActivity())
        }

        editEventBtn.setOnClickListener {
            // TODO: Логика добавления ивента в БД и на сервер
            if (eventNameTxt.text.isEmpty() || dateInput.text.isEmpty() || timeInput.text.isEmpty()) {
                Toast.makeText(requireContext(), "Необходимо заполнить все обязательные поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                event.name = eventNameTxt.text.toString()
                event.descr = eventTextTxt.text.toString()
                event.color = colorPicker.getColor()
                event.triggeredAt = LocalDateTime.of(date, time).toString()
                event.editedAt = LocalDateTime.now().toString()
                event.triggeredPeriod = repeatSelector.getSelectedOption()
                event.isPeriodic = event.triggeredPeriod > 0

                dbHelper.updateEvent(event)

                FragmentSwitcher.backPress(requireActivity())
            } catch (e: Exception){
                Toast.makeText(requireContext(), "Некорректные данные", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }

        return view
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(requireContext(), R.style.DialogTheme, { _, selectedYear, selectedMonth, selectedDay ->
            date = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
            updateDateText()
        }, this.date.year, this.date.month.value - 1, this.date.dayOfMonth)

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
                val curTime = LocalTime.now()
                val newTime = LocalTime.of(selectedHour, selectedMinute)

                if (newTime.isBefore(curTime))
                {
                    Toast.makeText(requireContext(), "Нельзя выбрать время, которое уже прошло", Toast.LENGTH_SHORT).show()
                    return@TimePickerDialog
                }

                time = LocalTime.of(selectedHour, selectedMinute)
                timeInput.setText(TimeFormatHelper.toString(time))
            },
            this.time.hour, this.time.minute, true
        )

        timePickerDialog.show()
    }

    private fun updateTimeText(){
        timeInput.setText(TimeFormatHelper.toString(time))
    }
}