package com.example.unifocus.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unifocus.R
import com.example.unifocus.data.models.task.Task
import java.time.format.DateTimeFormatter

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    class TaskViewHolder(
        itemView: View,
        private val onTaskClick: (Task) -> Unit,
        private val onDeleteClick: (Task) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.taskName)
        private val roomView: TextView = itemView.findViewById(R.id.taskRoom)
        private val deadlineView: TextView = itemView.findViewById(R.id.taskDeadline)
        private val teacherView: TextView = itemView.findViewById(R.id.taskTeacher)
        private val groupView: TextView = itemView.findViewById(R.id.taskGroup)
        private val editTask: ImageButton = itemView.findViewById(R.id.edit_button)
        private val deleteTask: ImageButton = itemView.findViewById(R.id.edit_button)
        private val taskLayout: LinearLayout = itemView.findViewById(R.id.task_layout)

        fun bind(task: Task) {
            nameView.text = task.name
//            descView.text = task.description ?: ""
            roomView.text = task.room ?: ""
            deadlineView.text = task.deadline?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            teacherView.text = task.teacher ?: ""
            groupView.text = task.group ?: ""

            taskLayout.setOnClickListener {
                onTaskClick(task)
            }

            deleteTask.setOnClickListener {
                onDeleteClick(task)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view, onTaskClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}