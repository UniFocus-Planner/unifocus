package com.example.unifocus.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.unifocus.R
import com.example.unifocus.data.models.calendar_day.CalendarDay
import android.graphics.Color
import androidx.recyclerview.widget.DiffUtil

class CalendarAdapter(
    initialDays: List<CalendarDay>,
    private val onDayClick: (CalendarDay) -> Unit // Добавляем колбэк
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    private var selectedPosition = -1
    private var todayPosition = -1
    private var days: MutableList<CalendarDay> = initialDays.toMutableList()

    init {
        // Инициализация позиции сегодняшнего дня
        days.forEachIndexed { index, day ->
            if (day.isToday) {
                todayPosition = index
                selectedPosition = index
            }
        }
    }

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayNumber: TextView = view.findViewById(R.id.day_number)
        val dayHighlight: View = view.findViewById(R.id.day_highlight)
        val daySelected: View = view.findViewById(R.id.day_selected)
        val taskDot: View = view.findViewById(R.id.task_dot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]

        holder.dayNumber.text = day.number.toString()
        holder.dayHighlight.visibility = if (day.isToday) View.VISIBLE else View.INVISIBLE

        // Обновление видимости маркеров
        holder.daySelected.visibility = if (position == selectedPosition && !day.isToday) View.VISIBLE else View.INVISIBLE
        holder.taskDot.visibility = if (day.hasTasks) View.VISIBLE else View.GONE

        // Цвет текста
        when {
            day.isToday -> holder.dayNumber.setTextColor(Color.WHITE)
            position == selectedPosition -> holder.dayNumber.setTextColor(Color.WHITE)
            else -> holder.dayNumber.setTextColor(
                if (day.isCurrentMonth) Color.BLACK
                else ContextCompat.getColor(holder.itemView.context, R.color.gray)
            )
        }

        holder.itemView.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION && currentPosition != selectedPosition) {
                selectedPosition = currentPosition
                onDayClick(days[currentPosition]) // Используем актуальную позицию
                notifyDataSetChanged()
            }
        }
    }

    fun updateDays(newDays: List<CalendarDay>) {
        val diffResult = DiffUtil.calculateDiff(CalendarDiffUtil(days, newDays))
        days.clear()
        days.addAll(newDays)

        // Обновляем позиции после изменения данных
        days.forEachIndexed { index, day ->
            if (day.isToday) {
                todayPosition = index
                if (selectedPosition == -1) selectedPosition = index
            }
        }

        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount() = days.size
}