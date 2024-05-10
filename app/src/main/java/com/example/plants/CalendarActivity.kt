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


import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.CalendarView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.plants.adapter.WorkTypeAdapter
import com.example.plants.databinding.ActivityCalendarBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class SelectedDay(
    val workType: String = "",
    val dayDate: String = "",
    val vegetableId: Int = -1
)


class CalendarActivity : AppCompatActivity() {



    private lateinit var binding: ActivityCalendarBinding
    private lateinit var spinner: Spinner
    private lateinit var calendarView: CalendarView
    private var currentYearMonth = MutableStateFlow(YearMonth.now())
    private val selectedDaysState = MutableStateFlow(listOf<SelectedDay>())

    private val dbHelper by lazy { DBHelper.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val currentVegetableId = intent.extras?.getInt(VEG_ID_PARAM) ?: 0

        selectedDaysState.value = dbHelper.getSelectedDays()

        spinner = binding.spinnerWorkType
        val workTypes = listOf(
            "Посадка/посев",
            "Подкормка",
            "Полив",
            "Обрезка",
            "Обработка от болезней/вредителей",
            "Сбор урожая"
        )

        val colorsArray = listOf(
            Color.parseColor("#a95e13"),
            Color.parseColor("#EDD900"),
            Color.parseColor("#b3dcfd"),
            Color.parseColor("#44944A"),
            Color.parseColor("#6D3F5B"),
            Color.parseColor("#9B2D30"),).toTypedArray()

        val workColors = workTypes.zip(colorsArray)

        val adapter = WorkTypeAdapter(this, workTypes, spinner)
        spinner.adapter = adapter
        spinner.setSelection(0)
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedDaysState.value = selectedDaysState.value + SelectedDay("", "")
                selectedDaysState.value = selectedDaysState.value - SelectedDay("", "")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }


        val calendarView = binding.calendarView

        lifecycleScope.launch {
            selectedDaysState.collect { selectedDays ->

                calendarView.dayBinder = getDayBinder(
                    onDayClick = { dayDate ->
                        val selectedDay = SelectedDay(
                            workTypes[spinner.selectedItemId.toInt()],
                            dayDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            vegetableId = currentVegetableId
                        )
                        if(selectedDays.any { it == selectedDay }){
                            selectedDaysState.value = selectedDays - selectedDay
                        }
                        else {
                            selectedDaysState.value = selectedDays + selectedDay
                        }
                    },
                    checkSelected = { calendarDay ->
                        selectedDays.any {
                            it.dayDate == calendarDay.date.format(
                                DateTimeFormatter.ISO_LOCAL_DATE
                            ) && it.workType == workTypes[spinner.selectedItemId.toInt()] && it.vegetableId == currentVegetableId
                        }
                    },
                    color = workColors[spinner.selectedItemId.toInt()].second
                )
            }
        }


        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(200)
        val endMonth = currentMonth.plusMonths(200)
        val firstDayOfWeek = firstDayOfWeekFromLocale()
        calendarView.setup(startMonth, endMonth, firstDayOfWeek)

        binding.arrowPrevious.setOnClickListener {
            currentYearMonth.value = currentYearMonth.value.minusMonths(1)
        }

        binding.arrowNext.setOnClickListener {
            currentYearMonth.value = currentYearMonth.value.plusMonths(1)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                currentYearMonth.collect { yearMonth ->
                    calendarView.scrollToMonth(yearMonth)

                    binding.calendarMonth.text = getString(
                        R.string.month_view, yearMonth.month.getDisplayName(
                            TextStyle.SHORT,
                            Locale.getDefault()
                        ), yearMonth.year.toString()
                    )
                }
            }
        }

        binding.buttonSave.setOnClickListener {
            if(dbHelper.addSelectedDays(selectedDaysState.value)){
                Toast.makeText(this, getString(R.string.save_cal), Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, getString(R.string.no_save_cal), Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class DayViewContainer(view: View) : ViewContainer(view) {
    val textView1 = view.findViewById<TextView>(R.id.calendarDayText1)!!
    val textView2 = view.findViewById<TextView>(R.id.calendarDayText2)!!
    val cardView = view.findViewById<CardView>(R.id.card_view)!!
}

fun getDayBinder(
    onDayClick: (LocalDate) -> Unit,
    checkSelected: (CalendarDay) -> (Boolean),
    color: Int
) =
    object : MonthDayBinder<DayViewContainer> {
        override fun create(view: View) = DayViewContainer(view)

        override fun bind(container: DayViewContainer, data: CalendarDay) = with(container) {
            textView1.text = data.date.dayOfMonth.toString()
            textView2.text = data.date.dayOfMonth.toString()

            if (checkSelected(data) && data.position == DayPosition.MonthDate) {
                cardView.visibility = View.VISIBLE
                textView2.visibility = View.INVISIBLE
                cardView.setCardBackgroundColor(color)
            } else {
                cardView.visibility = View.INVISIBLE
            }

            if (data.position != DayPosition.MonthDate) {
                textView1.setTextColor(Color.GRAY)
                textView2.setTextColor(Color.GRAY)
            }

            container.view.setOnClickListener {
                if (data.position == DayPosition.MonthDate) {
                    onDayClick(data.date)
                }
            }
        }
    }