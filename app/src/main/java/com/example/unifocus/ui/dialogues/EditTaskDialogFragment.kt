package com.example.unifocus.ui.dialogues

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.DialogFragment
import com.example.unifocus.R
import com.example.unifocus.data.models.task.Task
import com.example.unifocus.data.models.task.TaskType
import java.util.Calendar
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import com.example.unifocus.domain.ScheduleItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class EditTaskDialogFragment : DialogFragment() {
    private var selectedDeadline: LocalDateTime? = null

    interface OnTaskUpdatedListener {
        fun onTaskUpdated(task: Task)
    }

    private var listener: OnTaskUpdatedListener? = null

    fun setOnTaskUpdatedListener(listener: OnTaskUpdatedListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view: View = LayoutInflater.from(context).inflate(R.layout.dialog_edit_task, null)

        val nameEditText = view.findViewById<TextView>(R.id.name_task_edit)
        val descEditText = view.findViewById<TextView>(R.id.description_task)
        val notes = view.findViewById<TextView>(R.id.notes_task)
        val teacher = view.findViewById<TextView>(R.id.teacher_task)

        val taskId = requireArguments().getInt("id")
        val name = requireArguments().getString("name") ?: ""
        val desc = requireArguments().getString("description")
        val teach = requireArguments().getString("teacher")
        val typeOrdinal = requireArguments().getInt("taskTypeOrdinal")
        val addInfo = requireArguments().getString("additionalInformation")

        nameEditText.setText(name)
        descEditText.setText(desc ?: "")
        teacher.setText(teach ?: "")
        notes.setText(addInfo ?: "")

        val saveButton = view.findViewById<Button>(R.id.button_task)

        val deadlineText = view.findViewById<TextView>(R.id.deadlineText)

        // Получаем deadline как LocalDateTime из аргументов
        selectedDeadline = arguments?.getSerializable("deadline") as? LocalDateTime

        selectedDeadline?.let {
            deadlineText.text = it.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        }

        deadlineText.setOnClickListener {
            showDateTimePicker(deadlineText)
        }

        saveButton.setOnClickListener {
            val updatedTask = Task(
                id = taskId,
                name = nameEditText.text.toString(),
                description = descEditText.text.toString(),
                deadline = selectedDeadline,
                taskType = TaskType.values()[typeOrdinal],
                room = requireArguments().getString("room"),
                number = requireArguments().getInt("number"),
                teacher = teacher.text.toString(),
                selected = requireArguments().getBoolean("selected"),
                weekType = (requireArguments().getSerializable("weekType") as? ScheduleItem.WeekType),
                group = requireArguments().getString("group"),
                schedule1 = requireArguments().getString("schedule1"),
                schedule2 = requireArguments().getString("schedule2"),
                additionalInformation = notes.text.toString()
            )
            listener?.onTaskUpdated(updatedTask)
            dismiss()
        }

        val closeButton = view.findViewById<AppCompatImageButton>(R.id.close_button_task_edit)
        closeButton.setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

    private fun showDateTimePicker(deadlineText: TextView) {
        val calendar = Calendar.getInstance()
        selectedDeadline?.let {
            calendar.set(Calendar.YEAR, it.year)
            calendar.set(Calendar.MONTH, it.monthValue - 1)
            calendar.set(Calendar.DAY_OF_MONTH, it.dayOfMonth)
            calendar.set(Calendar.HOUR_OF_DAY, it.hour)
            calendar.set(Calendar.MINUTE, it.minute)
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        selectedDeadline = LocalDateTime.of(year, month + 1, day, hour, minute)
                        deadlineText.text = selectedDeadline?.format(
                            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                        )
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    companion object {
        fun newInstance(task: Task): EditTaskDialogFragment {
            val fragment = EditTaskDialogFragment()
            val args = Bundle().apply {
                putInt("id", task.id)
                putString("name", task.name)
                putString("description", task.description)
                putInt("taskTypeOrdinal", task.taskType.ordinal)
                putSerializable("deadline", task.deadline)
                putString("room", task.room)
                putString("teacher", task.teacher)
                putSerializable("weekType", task.weekType)
                putSerializable("number", task.number)
                putSerializable("group", task.group)
                putSerializable("selected", task.selected)
                putSerializable("schedule1", task.schedule1)
                putSerializable("schedule2", task.schedule2)
                putString("additionalInformation", task.additionalInformation)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
