//package com.example.plants
//
//import android.graphics.Color
//import android.os.Bundle
//import android.view.View
//import android.view.ViewGroup
//import android.widget.AdapterView
//import android.widget.CalendarView
//import android.widget.Spinner
//import androidx.appcompat.app.AppCompatActivity
//import com.example.plants.adapter.WorkTypeAdapter
//import com.example.plants.databinding.ActivityCalendarBinding
//import java.util.*
//
//class CalendarActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityCalendarBinding
//    private lateinit var spinner: Spinner
//    private lateinit var calendarView: CalendarView
//    private var selectedWorkType: String = ""
//    private lateinit var dbHelper: DBHelper
//
//   override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityCalendarBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Инициализация DBHelper
//        dbHelper = DBHelper.getInstance(this)
//
//        spinner = binding.spinnerWorkType
//        val workTypes = listOf(
//            "Посадка/посев",
//            "Подкормка",
//            "Полив",
//            "Обрезка",
//            "Обработка от болезней/вредителей",
//            "Сбор урожая"
//        )
//
//        val adapter = WorkTypeAdapter(this, workTypes, spinner)
//        spinner.adapter = adapter
//
//        // Устанавливаем дефолтное значение
//        spinner.setSelection(0)
//
//        adapter.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                selectedWorkType = workTypes[position]
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                // Здесь можно выполнить действия, если ничего не выбрано
//            }
//        })
//
//        calendarView = binding.calendarView
//        // Установка обработчика кликов по датам в календаре
//        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
//            val selectedDate = Calendar.getInstance().apply {
//                set(year, month, dayOfMonth)
//            }
//            val date = "$dayOfMonth.${month + 1}.$year" // Преобразуем в строку формата "день.месяц.год"
//
//            // Проверка, есть ли такая дата уже в базе данных
//            val selectedDates = dbHelper.getSelectedDates()
//            if (selectedDates.contains(date)) {
//                // Если дата уже выбрана, удаляем ее из базы данных и календаря
//                dbHelper.deleteSelectedDate(date)
//                clearDayBackgroundColor(selectedDate)
//            } else {
//                // Если дата еще не выбрана, добавляем ее в базу данных и календарь
//                dbHelper.addSelectedDate(date, selectedWorkType)
//                highlightDayBackgroundColor(selectedDate)
//            }
//        }
//
//        // Подсветка выбранных дат в календаре
//        highlightSelectedDates()
//    }
//
//    private fun highlightSelectedDates() {
//        val selectedDates = dbHelper.getSelectedDates()
//        for (dateString in selectedDates) {
//            val dateParts = dateString.split(".")
//            val dayOfMonth = dateParts[0].toInt()
//            val month = dateParts[1].toInt() - 1 // Calendar.MONTH начинается с 0
//            val year = dateParts[2].toInt()
//            val selectedDate = Calendar.getInstance().apply {
//                set(year, month, dayOfMonth)
//            }
//            highlightDayBackgroundColor(selectedDate)
//        }
//    }
//
//    private fun highlightDayBackgroundColor(selectedDate: Calendar) {
//        val selectedDay = selectedDate.get(Calendar.DAY_OF_MONTH)
//        val selectedMonth = selectedDate.get(Calendar.MONTH)
//        val selectedYear = selectedDate.get(Calendar.YEAR)
//
//        (calendarView.getChildAt(0) as? ViewGroup)?.let { monthView ->
//            (monthView.getChildAt(selectedMonth) as? ViewGroup)?.let { weekView ->
//                (weekView.getChildAt(selectedDate.get(Calendar.WEEK_OF_MONTH)) as? ViewGroup)?.let { dayView ->
//                    dayView.getChildAt(selectedDay - 1)?.setBackgroundColor(Color.RED)
//                }
//            }
//        }
//    }
//
//    private fun clearDayBackgroundColor(selectedDate: Calendar) {
//        val selectedDay = selectedDate.get(Calendar.DAY_OF_MONTH)
//        val selectedMonth = selectedDate.get(Calendar.MONTH)
//        val selectedYear = selectedDate.get(Calendar.YEAR)
//
//        (calendarView.getChildAt(0) as? ViewGroup)?.let { monthView ->
//            (monthView.getChildAt(selectedMonth) as? ViewGroup)?.let { weekView ->
//                (weekView.getChildAt(selectedDate.get(Calendar.WEEK_OF_MONTH)) as? ViewGroup)?.let { dayView ->
//                    dayView.getChildAt(selectedDay - 1)?.setBackgroundColor(Color.WHITE)
//                }
//            }
//        }
//    }
//}

package com.example.plants


import android.os.Bundle
import android.view.View

import android.widget.CalendarView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.plants.adapter.WorkTypeAdapter

import com.example.plants.databinding.ActivityCalendarBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.YearMonth


class DayViewContainer(view: View) : ViewContainer(view) {
    val textView = view.findViewById<TextView>(R.id.calendarDayText)

}

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private lateinit var spinner: Spinner
    private lateinit var calendarView: CalendarView
    private var selectedWorkType: String = ""
    private lateinit var dbHelper: DBHelper


   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация DBHelper
        dbHelper = DBHelper.getInstance(this)

        spinner = binding.spinnerWorkType
        val workTypes = listOf(
            "Посадка/посев",
            "Подкормка",
            "Полив",
            "Обрезка",
            "Обработка от болезней/вредителей",
            "Сбор урожая"
        )

        val adapter = WorkTypeAdapter(this, workTypes, spinner)
        spinner.adapter = adapter

        // Устанавливаем дефолтное значение
        spinner.setSelection(0)

        val calendarView = binding.calendarView

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

           // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, data: CalendarDay) {
               container.textView.text = data.date.dayOfMonth.toString()
           }
       }
       val currentMonth = YearMonth.now()
       val startMonth = currentMonth.minusMonths(100)
       val endMonth = currentMonth.plusMonths(100)
       val firstDayOfWeek = firstDayOfWeekFromLocale()
       calendarView.setup(startMonth, endMonth, firstDayOfWeek)
    }
}
