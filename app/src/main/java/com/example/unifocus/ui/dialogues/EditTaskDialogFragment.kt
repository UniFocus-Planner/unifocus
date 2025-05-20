package com.example.unifocus.ui.dialogues

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
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
import com.example.unifocus.domain.ScheduleItem
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale


class EditTaskDialogFragment : DialogFragment() {
    private var selectedDeadline: LocalDateTime? = null
    private var selectedNotificationTime: Calendar? = null

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
        val notes = view.findViewById<TextView>(R.id.notes_task)
        val teacher = view.findViewById<TextView>(R.id.teacher_task)

        val taskId = requireArguments().getInt("id")
        val name = requireArguments().getString("name") ?: ""
        val teach = requireArguments().getString("teacher")
        val typeOrdinal = requireArguments().getInt("taskTypeOrdinal")
        val addInfo = requireArguments().getString("additionalInformation")

        nameEditText.setText(name)
        teacher.setText(teach ?: "")
        notes.setText(addInfo ?: "")

        val saveButton = view.findViewById<Button>(R.id.button_task)

        val deadlineText = view.findViewById<TextView>(R.id.deadlineText)
        val notificationText = view.findViewById<TextView>(R.id.notificationText)

        selectedDeadline = arguments?.getSerializable("deadline") as? LocalDateTime

        selectedDeadline?.let {
            deadlineText.text = it.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        }

        deadlineText.setOnClickListener {
            showDateTimePickerForDeadline(deadlineText)
        }

        // Для notificationTime
        selectedNotificationTime = arguments?.getSerializable("notificationTime") as? Calendar

        selectedNotificationTime?.let {
            notificationText.text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(it.time)
        }

        notificationText.setOnClickListener {
            showDateTimePickerForNotification(notificationText)
        }

        saveButton.setOnClickListener {
            val taskNewName = nameEditText.text.toString().trim()
            if (taskNewName.isNotEmpty()) {
                val updatedTask = Task(
                    id = taskId,
                    name = taskNewName,
                    deadline = selectedDeadline,
                    notificationTime = selectedNotificationTime,
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
            } else {
                nameEditText.error = "Введите название задачи"
            }
        }

        val closeButton = view.findViewById<AppCompatImageButton>(R.id.close_button_task_edit)
        closeButton.setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

    private fun showDateTimePickerForDeadline(deadlineText: TextView) {
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

    private fun showDateTimePickerForNotification(notificationText: TextView) {
        val calendar = selectedNotificationTime ?: Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        selectedNotificationTime = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, day)
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                        }

                        notificationText.text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                            .format(selectedNotificationTime?.time ?: Date())
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
                putInt("taskTypeOrdinal", task.taskType.ordinal)
                putSerializable("deadline", task.deadline)
                putSerializable("notificationTime", task.notificationTime)
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
