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

class CalendarAdapter(initialDays: List<CalendarDay>) :
    RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    private var days: MutableList<CalendarDay> = initialDays.toMutableList()

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayNumber: TextView = view.findViewById(R.id.day_number)
        val dayHighlight: View = view.findViewById(R.id.day_highlight)
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

        // Цвет текста
        if (day.isToday) {
            holder.dayNumber.setTextColor(Color.WHITE) // Текущий день - белый
        } else {
            holder.dayNumber.setTextColor(
                if (day.isCurrentMonth) Color.BLACK
                else ContextCompat.getColor(holder.itemView.context, R.color.gray)
            )
        }
    }

    fun updateDays(newDays: List<CalendarDay>) {
        val diffResult = DiffUtil.calculateDiff(CalendarDiffUtil(days, newDays))
        days.clear()
        days.addAll(newDays)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount() = days.size
}