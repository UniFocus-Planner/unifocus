package com.example.unifocus.ui.view

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatImageButton
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.unifocus.R
import com.example.unifocus.UniFocusApp
import com.example.unifocus.data.models.task.Task
import com.example.unifocus.data.models.task.TaskType
import com.example.unifocus.ui.adapter.ScheduleAdapter
import com.example.unifocus.ui.adapter.TaskAdapter
import com.example.unifocus.ui.viewmodels.UniFocusViewModel
import com.example.unifocus.ui.viewmodels.UniFocusViewModelFactory

class ProfileScreen : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleAdapter
    private lateinit var viewModel: UniFocusViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.profile, container, false)

        val repository = (requireActivity().application as UniFocusApp).repository
        val factory = UniFocusViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UniFocusViewModel::class.java]
        adapter = ScheduleAdapter()
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.adapter = adapter

        val addButton: AppCompatImageButton = view.findViewById(R.id.schedule_add_button)

        addButton.setOnClickListener {
            viewModel.createSchedule(
                name = "Расписание",
                tasks = listOf()
            )
        }

        //val addTask: Button = view.findViewById(R.id.schedule_add_button)
        //addTask.setOnClickListener {
        //    //showCreateTaskDialog()
        //}

        viewModel.schedules.observe(viewLifecycleOwner, { schedules ->
            adapter.submitList(schedules)
        })

        return view
    }

   //private fun showCreateTaskDialog() {
   //    val dialog = CreateTaskDialog.newInstance()
   //    dialog.setOnTaskCreatedListener(this)
   //    dialog.show(parentFragmentManager, "CreateTaskDialog")
   //}

   //override fun onTaskCreated(task: Task) {
   //    viewModel.addTask(task)
   //}
}