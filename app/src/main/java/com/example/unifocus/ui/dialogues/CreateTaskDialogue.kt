package com.example.unifocus.ui.dialogues

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.example.unifocus.R
import com.example.unifocus.data.models.task.Task
import com.example.unifocus.data.models.task.TaskType
import com.example.unifocus.domain.TaskFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar
import java.util.Locale
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CreateTaskDialog : BottomSheetDialogFragment() {
    private var listener: OnTaskCreatedListener? = null
    private var selectedDeadline: LocalDateTime? = null

    interface OnTaskCreatedListener {
        fun onTaskCreated(task: Task)
    }

    fun setOnTaskCreatedListener(listener: OnTaskCreatedListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.task_create_dialogue, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nameInput = view.findViewById<TextView>(R.id.name_task_edit)
        val descInput = view.findViewById<TextView>(R.id.description_task)
        val additionalInformation = view.findViewById<TextView>(R.id.notes_task)
        val createButton = view.findViewById<Button>(R.id.button_task)
        val deadlineText = view.findViewById<TextView>(R.id.deadlineText)
        val teacherText = view.findViewById<TextView>(R.id.teacher_task)

        selectedDeadline?.let {
            deadlineText.text = it.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault()))
        }

        deadlineText.setOnClickListener {
            showDateTimePicker(deadlineText)
        }

        createButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            if (name.isNotEmpty()) {
                val task = TaskFactory.createTask(
                    name = name,
                    description = descInput.text.toString(),
                    deadline = selectedDeadline,
                    taskType = TaskType.CLASS,
                    selected = true,
                    teacher = teacherText.text.toString(),
                    group = "",
                    schedule = "",
                    additionalInformation = additionalInformation.text.toString()
                )
                listener?.onTaskCreated(task)
                dismiss()
            } else {
                nameInput.error = "Введите название задачи"
            }
        }

        val closeButton = view.findViewById<ImageButton>(R.id.close_button_task_edit)
        closeButton.setOnClickListener {
            dismiss()
        }
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
                        // Создаем LocalDateTime из выбранных значений
                        selectedDeadline = LocalDateTime.of(
                            year,
                            month + 1, // Месяцы в LocalDateTime от 1 до 12
                            day,
                            hour,
                            minute
                        )
                        deadlineText.text = selectedDeadline?.format(
                            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())
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

    override fun onStart() {
        super.onStart()

        dialog?.let { dialog ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT

            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet)
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    companion object {
        fun newInstance() = CreateTaskDialog()
    }
}