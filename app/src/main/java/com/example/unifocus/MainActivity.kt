package com.example.unifocus

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.unifocus.data.database.UniFocusDatabase
import com.example.unifocus.data.models.task.TaskType
import com.example.unifocus.data.repository.UniFocusRepository
import com.example.unifocus.domain.ScheduleFactory
import com.example.unifocus.domain.TaskFactory
import com.example.unifocus.ui.adapter.TaskAdapter
import com.example.unifocus.ui.viewmodels.UniFocusViewModel
import com.example.unifocus.ui.viewmodels.UniFocusViewModelFactory

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter

    private val viewModel: UniFocusViewModel by viewModels {
        UniFocusViewModelFactory(UniFocusRepository(UniFocusDatabase.getDatabase(this)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_layout)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        adapter = TaskAdapter()
        recyclerView.adapter = adapter

        viewModel.classTasks.observe(this) { tasks ->
            println("Задач обновлено: ${tasks?.size}")
            adapter.submitList(tasks)
        }

        findViewById<Button>(R.id.addButton).also {
            it.setOnClickListener {
                viewModel.createTask("BYE", taskType = TaskType.CLASS)
            }
        }

        findViewById<Button>(R.id.deleteButton).also {
            it.setOnClickListener {
                viewModel.deleteAllTasks()
            }
        }

        addTestData()
    }

    private fun addTestTask() {
        println("Creating hello task")
        println(viewModel.classTasks.value)
        viewModel.createTask(
            name = "HELLO",
            taskType = TaskType.CLASS)
    }

    private fun addTestData() {
        CoroutineScope(Dispatchers.IO).launch {
            val schedule = ScheduleFactory.createSchedule("БПИ-22-РП-3")

            val tasks = listOf(
                TaskFactory.createTask(
                    name = "Математика",
                    description = "Лекция по математике",
                    deadline = System.currentTimeMillis() + 86400000,
                    taskType = TaskType.CLASS
                ),
                TaskFactory.createTask(
                    name = "История",
                    description = "Семинар по истории",
                    deadline = System.currentTimeMillis() + 172800000,
                    taskType = TaskType.CLASS
                )
            )

            viewModel.addSchedule(schedule, tasks)
        }
    }
}
