package com.example.remindmeapp.fragments

import android.app.AlertDialog
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

class EditEventFragment : Fragment() {

    private lateinit var date : LocalDate
    private lateinit var time : LocalTime

    private lateinit var event : Event
    private lateinit var dbHelper: DbHelper

    private lateinit var dateInput : EditText
    private lateinit var timeInput : EditText

    private lateinit var unsubscribe : () -> Unit

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

        dbHelper = DbHelper(requireContext(), null)
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
        eventNameTxt.setText(event.name)
        eventTextTxt.setText(event.descr)

        val dateTime = DateTimeFormatHelper.parseZone(event.triggeredAt)
        date = dateTime.toLocalDate()
        time = dateTime.toLocalTime()

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
            showDeleteConfirmationDialog(event.id)
        }

        unsubscribe = FragmentSwitcher.onEventTriggered {
            if (context != null) {
                updateEventInfo()
            }
        }

        editEventBtn.setOnClickListener {
            if (eventNameTxt.text.isEmpty() || dateInput.text.isEmpty() || timeInput.text.isEmpty()) {
                Toast.makeText(requireContext(), "Необходимо заполнить все обязательные поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                event.name = eventNameTxt.text.toString()
                event.descr = eventTextTxt.text.toString()
                event.color = colorPicker.getColor()
                event.triggeredAt = DateTimeFormatHelper.toZoneString(LocalDateTime.of(date, time))
                event.editedAt = DateTimeFormatHelper.toZoneString(LocalDateTime.now())
                event.triggeredPeriod = repeatSelector.getSelectedOption()
                event.isPeriodic = event.triggeredPeriod > 0

                // TODO: Логика обновления ивента в БД и на сервере
                dbHelper.updateEventServer(event)
                dbHelper.updateEvent(event)

                FragmentSwitcher.updateEvents()
                FragmentSwitcher.backPress(requireActivity())
            } catch (e: Exception){
                println(e)
                Toast.makeText(requireContext(), "Некорректные данные", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }

        return view
    }

    override fun onDestroyView() {
        unsubscribe()
        super.onDestroyView()
    }

    private fun showDeleteConfirmationDialog(eventId: Int) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Подтверждение удаления")
        alertDialogBuilder.setMessage("Вы действительно хотите удалить это событие?")

        alertDialogBuilder.setPositiveButton("Да") { dialog, _ ->
            // Удаляем событие
            dbHelper.deleteEventServer(event)
            dbHelper.deleteEventById(eventId)
            dialog.dismiss() // Закрываем диалог
            FragmentSwitcher.updateEvents()
            // FragmentSwitcher.backPress(requireActivity())
        }

        alertDialogBuilder.setNegativeButton("Нет") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
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

    private fun updateEventInfo(){
        if (requireContext() == null)
            return

        if (dbHelper.getEventById(event.id) == null) {
            Toast.makeText(requireContext(), "Событие уже отработало и было удалено", Toast.LENGTH_SHORT).show()
            FragmentSwitcher.backPress(requireActivity())
        }
    }
}