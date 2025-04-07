package com.example.unifocus.ui.view

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.unifocus.R
import com.example.unifocus.data.database.UniFocusDatabase
import com.example.unifocus.data.repository.UniFocusRepository
import com.example.unifocus.ui.adapter.TaskAdapter
import com.example.unifocus.ui.viewmodels.UniFocusViewModel
import com.example.unifocus.ui.viewmodels.UniFocusViewModelFactory

class TodayTasksScreen : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.today_tasks, container, false)
        adapter = TaskAdapter()
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.adapter = adapter

       //view.findViewById<Button>(R.id.addButton).also {
       //    it.setOnClickListener {
       //        viewModel.createTask("BYE", taskType = TaskType.CLASS)
       //    }
       //}

       //findViewById<Button>(R.id.deleteButton).also {
       //    it.setOnClickListener {
       //        viewModel.deleteAllTasks()
       //    }
       //}

        return view
    }
}