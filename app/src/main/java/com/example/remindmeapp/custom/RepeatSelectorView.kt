package com.example.remindmeapp

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat

class RepeatSelectorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val buttons: List<Button>
    private var selectedButton: Button? = null
    private val predefinedList: List<Int> = listOf(0, 1, 7, 30, 365)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_repeat_selector, this, true)

        // Инициализация кнопок
        val buttonNotRepeat: Button = findViewById(R.id.buttonNotRepeat)
        val buttonDayRepeat: Button = findViewById(R.id.buttonDayRepeat)
        val buttonWeekRepeat: Button = findViewById(R.id.buttonWeekRepeat)
        val buttonMonthRepeat: Button = findViewById(R.id.buttonMonthRepeat)
        val buttonYearRepeat: Button = findViewById(R.id.buttonYearRepeat)

        buttons = listOf(buttonNotRepeat, buttonDayRepeat, buttonWeekRepeat, buttonMonthRepeat, buttonYearRepeat)

        // Назначаем обработчик клика на каждую кнопку
        buttons.forEach { button ->
            button.setOnClickListener { handleButtonClick(button) }
        }

        handleButtonClick(buttons[0])
    }

    private fun handleButtonClick(selectedButton: Button) {
        // Сброс стилей всех кнопок
        buttons.forEach { button ->
            button.backgroundTintList = ContextCompat.getColorStateList(context, R.color.light_green_back)
            button.setTextColor(ContextCompat.getColor(context, R.color.dark_gray))
        }
0
        // Установка стиля для выбранной кнопки
        selectedButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.light_green)
        selectedButton.setTextColor(ContextCompat.getColor(context, R.color.static_white))

        this.selectedButton = selectedButton
    }

    // Получение выбранного элемента
    fun getSelectedOption(): Int {
        return predefinedList[buttons.indexOf(selectedButton)]
    }

    fun setOption(periodTime : Int) {
        val index = predefinedList.indexOf(periodTime)

        if (index == -1)
            return

        handleButtonClick(buttons[index])
    }
}
