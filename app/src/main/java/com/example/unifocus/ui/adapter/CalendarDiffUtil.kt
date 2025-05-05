package com.example.unifocus.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.unifocus.data.models.calendar_day.CalendarDay

class CalendarDiffUtil(
    private val oldList: List<CalendarDay>,
    private val newList: List<CalendarDay>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
        return oldList[oldPos].date == newList[newPos].date
    }

    override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
        return oldList[oldPos] == newList[newPos]
    }
}