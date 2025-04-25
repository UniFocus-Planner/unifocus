package com.example.unifocus.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.unifocus.data.models.schedule.Schedule
import com.example.unifocus.data.models.task.Task
import com.example.unifocus.data.models.task.TaskType
import com.example.unifocus.data.repository.UniFocusRepository
import com.example.unifocus.domain.ScheduleFactory
import com.example.unifocus.domain.TaskFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UniFocusViewModel(private val repository: UniFocusRepository) : ViewModel() {

    val classTasks: LiveData<List<Task>> = repository.classTasks.asLiveData()
    val schedules: LiveData<List<Schedule>> = repository.schedules.asLiveData()
    val selectedSchedules: LiveData<List<Schedule>> = repository.selectedSchedules.asLiveData()

    fun createTask(
        name: String,
        description: String? = null,
        deadline: Long? = null,
        taskType: TaskType = TaskType.USER,
        additionalInformation: String? = null
    ) {
        addTask(TaskFactory.createTask(name, description, deadline, taskType, additionalInformation))
    }

    fun addTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun addTasks(tasks: List<Task>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTasks(tasks)
        }
    }

    fun deleteTaskByName(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTaskByName(name)
        }
    }

    fun deleteTaskById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTaskById(id)
        }
    }

    fun deleteAllTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllTasks()
        }
    }

    suspend fun getAllTasks(): List<Task> {
        return withContext(Dispatchers.IO) {
            repository.getAllTasks()
        }
    }

    fun createSchedule(
        name: String,
        tasks: List<Task>
    ) {
        addSchedule(ScheduleFactory.createSchedule(name), tasks)
    }

    fun addSchedule(schedule: Schedule, tasks: List<Task>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSchedule(schedule, tasks)
        }
    }

    fun selectSchedule(name: String, value:Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.selectSchedule(name,value)
        }
    }

    fun deleteSchedule(
        name: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSchedule(name)
        }
    }
}
