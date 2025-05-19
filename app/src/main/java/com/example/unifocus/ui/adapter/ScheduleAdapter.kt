package com.example.unifocus.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unifocus.R
import com.example.unifocus.data.models.schedule.Schedule

class ScheduleAdapter(
    private val onDeleteClick: (Schedule) -> Unit
) : ListAdapter<Schedule, ScheduleAdapter.ScheduleViewHolder>(ScheduleDiffCallback()), Filterable {

    private var originalList: List<Schedule> = emptyList()

    fun submitData(list: List<Schedule>) {
        originalList = list
        submitList(list)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase() ?: ""
                val filtered = if (query.isEmpty()) {
                    originalList
                } else {
                    originalList.filter {
                        it.groupName.lowercase().contains(query)
                    }
                }
                return FilterResults().apply { values = filtered }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                submitList(results?.values as? List<Schedule>)
            }
        }
    }


    class ScheduleViewHolder(
        scheduleView: View,
        private val onDeleteClick: (Schedule) -> Unit
    ) : RecyclerView.ViewHolder(scheduleView) {
        private val nameView: TextView = scheduleView.findViewById(R.id.schedule_title)
        private val deleteButton: ImageButton = scheduleView.findViewById(R.id.schedule_delete_button)

        fun bind(schedule: Schedule, isAddMode: Boolean) {
            nameView.text = schedule.groupName

            if (isAddMode) {
                deleteButton.setImageResource(android.R.drawable.ic_menu_add)
                deleteButton.contentDescription = "Добавить"
            } else {
                deleteButton.setImageResource(android.R.drawable.ic_menu_delete)
                deleteButton.contentDescription = "Удалить"
            }

            deleteButton.setOnClickListener {
                onDeleteClick(schedule)
                if (isAddMode) {
                    Toast.makeText(itemView.context, "Расписание добавлено", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false)
        return ScheduleViewHolder(view, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val isAddMode = originalList.isNotEmpty()
        holder.bind(getItem(position), isAddMode)
    }

    class ScheduleDiffCallback : DiffUtil.ItemCallback<Schedule>() {
        override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule) = oldItem.groupName == newItem.groupName
        override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule) = oldItem == newItem
    }
}