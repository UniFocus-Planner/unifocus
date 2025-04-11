package com.example.unifocus.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.unifocus.R
import com.example.unifocus.data.models.task.Task
import com.example.unifocus.data.models.task.TaskType
import com.example.unifocus.domain.TaskFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class CreateTaskDialog : BottomSheetDialogFragment() {
    private var listener: OnTaskCreatedListener? = null

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

        val nameInput = view.findViewById<TextView>(R.id.name_task_add)
        val descInput = view.findViewById<TextView>(R.id.description_task_add)
        val additionalInformation = view.findViewById<TextView>(R.id.notes_task_add)
        val createButton = view.findViewById<Button>(R.id.button_add_task)

        createButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            if (name.isNotEmpty()) {
                val task = TaskFactory.createTask(
                    name = nameInput.text.toString(),
                    description = descInput.text.toString(),
                    additionalInformation = additionalInformation.text.toString(),
                    taskType = TaskType.CLASS
                )
                listener?.onTaskCreated(task)
                dismiss()
            } else {
                nameInput.error = "Введите название задачи"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        fun newInstance() = CreateTaskDialog()
    }
}