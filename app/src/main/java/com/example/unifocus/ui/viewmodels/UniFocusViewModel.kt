package com.example.unifocus.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.unifocus.data.models.schedule.Schedule
import com.example.unifocus.data.models.task.Task
import com.example.unifocus.data.models.task.TaskType
import com.example.unifocus.data.repository.UniFocusRepository
import com.example.unifocus.domain.ScheduleFactory
import com.example.unifocus.domain.ScheduleItem
import com.example.unifocus.domain.TaskFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class UniFocusViewModel(private val repository: UniFocusRepository) : ViewModel() {

    val classTasks: LiveData<List<Task>> = repository.classTasks.asLiveData()
    val schedules: LiveData<List<Schedule>> = repository.schedules.asLiveData()
    val selectedSchedules: LiveData<List<Schedule>> = repository.selectedSchedules.asLiveData()
    val todaySelectedTasks: LiveData<List<Task>> = repository.todayTasks.asLiveData()
    val selectedTasks: LiveData<List<Task>> = repository.selectedTasks.asLiveData()

    fun toggleTaskSelection(taskId: Int, selected: Boolean) {
        viewModelScope.launch {
            repository.updateTaskSelection(taskId, selected)
        }
    }

    fun createTask(
        name: String,
        description: String? = null,
        deadline: LocalDateTime? = null,
        taskType: TaskType = TaskType.NONE,
        room: String? = null,
        teacher: String? = null,
        number: Int = 0,
        selected: Boolean = false,
        schedule: String = "",
        weekType: ScheduleItem.WeekType? = null,
        additionalInformation: String? = null
    ): Task {
        val task = TaskFactory.createTask( name = name,
            description = description,
            deadline = deadline,
            taskType = taskType,
            room = room,
            teacher = teacher,
            weekType = weekType,
            number = number,
            selected = selected,
            schedule = schedule,
            additionalInformation = additionalInformation
        )
        addTask(task)
        toggleTaskSelection(task.id, task.selected)
        return task
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
    ) {
        addSchedule(ScheduleFactory.createSchedule(name))
    }

    fun addSchedule(schedule: Schedule) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSchedule(schedule)
        }
    }

    fun selectSchedule(name: String, value:Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.selectSchedule(name,value)
        }
    }

    fun selectScheduleTasks(schedule: Schedule, value:Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("SelectScheduleTasks", schedule.tasks.toString())

            repository.selectTasks()
            schedule.tasks.forEach {
                repository.updateTaskSelection(it.id, value)
            }
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
