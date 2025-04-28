package com.example.unifocus.ui.view

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unifocus.R
import com.example.unifocus.ui.adapter.CalendarAdapter
import com.example.unifocus.data.models.calendar_day.CalendarDay
import java.util.Calendar
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Locale

class ScheduleScreen : Fragment() {
    private lateinit var calendarGrid: RecyclerView
    private lateinit var monthYearHeader: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.schedule, container, false)
        calendarGrid = view.findViewById(R.id.calendar_grid)
        monthYearHeader = view.findViewById(R.id.month_year_header)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Установка месяца и года
        val calendar = Calendar.getInstance()
        val monthNames = arrayOf(
            "Январь", "Февраль", "Март",
            "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь",
            "Октябрь", "Ноябрь", "Декабрь"
        )
        val month = monthNames[calendar.get(Calendar.MONTH)]
        val year = calendar.get(Calendar.YEAR)
        monthYearHeader.text = "$month $year"

        // Установка сегодняшней даты
        val todayHeader: TextView = view.findViewById(R.id.today_header)
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        todayHeader.text = "Сегодня: ${dateFormat.format(calendar.time)}"

        setupCalendar()
    }

    private fun setupCalendar() {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // Узнаем, на какой день недели приходится 1-е число месяца
        calendar.set(currentYear, currentMonth, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Добавляем пустые дни для выравнивания
        val days = mutableListOf<CalendarDay>()
        val daysBefore = (firstDayOfWeek - Calendar.SUNDAY + 7) % 7
        for (i in 0 until daysBefore) {
            days.add(CalendarDay(number = -1, isToday = false, isCurrentMonth = false))
        }

        // Добавляем дни текущего месяца
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..daysInMonth) {
            days.add(
                CalendarDay(
                    number = day,
                    isToday = day == currentDay,
                    isCurrentMonth = true
                )
            )
        }

        // Настройка RecyclerView
        val adapter = CalendarAdapter(days)
        calendarGrid.adapter = adapter
        calendarGrid.layoutManager = GridLayoutManager(requireContext(), 7)
    }
}