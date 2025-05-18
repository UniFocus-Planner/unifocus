package com.example.unifocus.data.models.calendar_day

import java.util.Calendar

data class CalendarDay(
    val number: Int,
    val isToday: Boolean,
    val isCurrentMonth: Boolean,
    val date: Calendar = Calendar.getInstance(),
    var hasTasks: Boolean = false // Новое поле для отметки задач
)