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

class EditTaskDialogFragment : DialogFragment() {

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

        val taskId = requireArguments().getInt("id")
        val name = requireArguments().getString("name") ?: ""
        val desc = requireArguments().getString("description")
        val typeOrdinal = requireArguments().getInt("taskTypeOrdinal")
        val deadline = requireArguments().getLong("deadline", 0L)
        val addInfo = requireArguments().getString("additionalInformation")

        nameEditText.setText(name)
        descEditText.setText(desc ?: "")
        notes.setText(addInfo ?: "")

        val saveButton = view.findViewById<Button>(R.id.button_task)
        saveButton.setOnClickListener {
            val updatedTask = Task(
                id = taskId,
                name = nameEditText.text.toString(),
                description = descEditText.text.toString(),
                deadline = deadline,
                taskType = TaskType.values()[typeOrdinal],
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

    companion object {
        fun newInstance(task: Task): EditTaskDialogFragment {
            val fragment = EditTaskDialogFragment()
            val args = Bundle().apply {
                putInt("id", task.id)
                putString("name", task.name)
                putString("description", task.description)
                putInt("taskTypeOrdinal", task.taskType.ordinal)
                putLong("deadline", task.deadline ?: 0L)
                putString("additionalInformation", task.additionalInformation)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
