package com.example.unifocus.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.unifocus.data.models.schedule.Schedule
import com.example.unifocus.data.models.task.Task
import com.example.unifocus.data.models.task.TaskType
import com.example.unifocus.data.repository.UniFocusRepository
import com.example.unifocus.domain.TaskFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UniFocusViewModel(private val repository: UniFocusRepository) : ViewModel() {

    val classTasks: LiveData<List<Task>> = repository.classTasks.asLiveData()

    fun addTask(name: String, description: String?, deadline: Long?, taskType: TaskType, additionalInformation: String?) {
        val task = TaskFactory.createTask(name, description, deadline, taskType, additionalInformation)
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTask(task)
        }
    }

    fun addSchedule(schedule: Schedule, tasks: List<Task>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSchedule(schedule, tasks)
        }
    }
}
