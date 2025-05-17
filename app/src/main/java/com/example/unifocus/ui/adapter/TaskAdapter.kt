package com.example.unifocus.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unifocus.R
import com.example.unifocus.data.models.task.Task
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date


class TaskAdapter(
    private val onTaskClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    class TaskViewHolder(
        itemView: View,
        private val onTaskClick: (Task) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.taskName)
        private val descView: TextView = itemView.findViewById(R.id.taskDesc)
        private val deadlineView: TextView = itemView.findViewById(R.id.taskDeadline)
        private val editTask: ImageButton = itemView.findViewById(R.id.edit_button)

        fun bind(task: Task) {
            nameView.text = task.name
            descView.text = task.description ?: "—"

            editTask.setOnClickListener {
                onTaskClick(task)
            }

            deadlineView.text = formatDeadline(task.deadline)
        }

        private fun formatDeadline(timestamp: Long?): String {
            if (timestamp == null) return "Дедлайн не установлен"
            return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view, onTaskClick)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}