package com.example.unifocus

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView

import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var taskInput: EditText
    private lateinit var addTaskButton: Button
    private lateinit var taskList: ListView
    private val tasks = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_layout)

        taskInput = findViewById(R.id.taskInput)
        addTaskButton = findViewById(R.id.addTaskButton)
        taskList = findViewById(R.id.taskList)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tasks)
        taskList.adapter = adapter

        addTaskButton.setOnClickListener {
            val taskText = taskInput.text.toString()
            if (taskText.isNotEmpty()) {
                tasks.add(taskText)
                adapter.notifyDataSetChanged()
                taskInput.text.clear()
            } else {
                //Toast.makeText(this, "Введите задачу", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
