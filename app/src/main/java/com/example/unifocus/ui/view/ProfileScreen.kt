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
import com.example.unifocus.ui.dialogues.CreateScheduleDialogue
import com.example.unifocus.ui.dialogues.CreateTaskDialog
import com.example.unifocus.ui.viewmodels.UniFocusViewModel
import com.example.unifocus.ui.viewmodels.UniFocusViewModelFactory
import java.util.UUID

class ProfileScreen : Fragment(), CreateScheduleDialogue.OnScheduleCreatedListener {
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
        adapter = ScheduleAdapter {
            viewModel.deleteSchedule(it.groupName)
        }

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.adapter = adapter

        view.findViewById<AppCompatImageButton?>(R.id.schedule_add_button).also {
            it.setOnClickListener {
                showCreateScheduleDialog()
            }
        }

        viewModel.selectedSchedules.observe(viewLifecycleOwner, { schedules ->
            adapter.submitList(schedules)
        })

        return view
    }

   private fun showCreateScheduleDialog() {
       val dialog = CreateScheduleDialogue.newInstance()
       dialog.setOnScheduleCreatedListener(this)
       dialog.show(parentFragmentManager, "CreateScheduleDialog")
   }

   override fun onScheduleCreated(task: Task) {
       viewModel.addTask(task)
   }
}