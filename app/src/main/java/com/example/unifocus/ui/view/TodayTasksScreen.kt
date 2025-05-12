package com.example.unifocus.ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.unifocus.R
import com.example.unifocus.UniFocusApp
import com.example.unifocus.data.models.task.Task
import com.example.unifocus.data.models.task.TaskType
import com.example.unifocus.ui.adapter.TaskAdapter
import com.example.unifocus.ui.decorators.VerticalSpaceItemDecoration
import com.example.unifocus.ui.dialogues.CreateScheduleDialogue
import com.example.unifocus.ui.dialogues.CreateTaskDialog
import com.example.unifocus.ui.dialogues.EditTaskDialogFragment
import com.example.unifocus.ui.viewmodels.UniFocusViewModel
import com.example.unifocus.ui.viewmodels.UniFocusViewModelFactory

class TodayTasksScreen : Fragment(), CreateTaskDialog.OnTaskCreatedListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private lateinit var viewModel: UniFocusViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.today_tasks, container, false)

        val repository = (requireActivity().application as UniFocusApp).repository
        val factory = UniFocusViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UniFocusViewModel::class.java]
        adapter = TaskAdapter {task -> showEditTaskDialog(task)}
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(VerticalSpaceItemDecoration(16))

        val addTask: Button = view.findViewById(R.id.add_task)
        addTask.setOnClickListener {
            showCreateTaskDialog()
        }

        val delTasksButton: Button = view.findViewById(R.id.deleteAllTasksButton)
        delTasksButton.setOnClickListener {
            viewModel.deleteAllTasks()
        }

        viewModel.todaySelectedTasks.observe(viewLifecycleOwner, { tasks ->
            adapter.submitList(tasks)
        })

        return view
    }

    private fun showCreateTaskDialog() {
        val dialog = CreateTaskDialog.newInstance()
        dialog.setOnTaskCreatedListener(this)
        dialog.show(parentFragmentManager, "CreateTaskDialog")
    }

    private fun showEditTaskDialog(task: Task) {
        val dialog = EditTaskDialogFragment.newInstance(task)
        dialog.setOnTaskUpdatedListener(object : EditTaskDialogFragment.OnTaskUpdatedListener {

            override fun onTaskUpdated(task: Task) {
                viewModel.updateTask(task)
            }
        })
        dialog.show(parentFragmentManager, "EditTaskDialog")
    }

    override fun onTaskCreated(task: Task) {
        viewModel.addTask(task)
    }
}