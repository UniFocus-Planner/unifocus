package com.example.unifocus.data.repository

import com.example.unifocus.data.database.UniFocusDatabase
import com.example.unifocus.data.models.schedule.Schedule
import com.example.unifocus.data.models.task.Task
import kotlinx.coroutines.flow.Flow

class UniFocusRepository(private val database: UniFocusDatabase) {
    private val taskDao = database.taskDao()
    private val scheduleDao = database.scheduleDao()

    val classTasks: Flow<List<Task>> = taskDao.getTasksByType("CLASS")
    val schedules: Flow<List<Schedule>> = scheduleDao.getSchedules()
    val selectedSchedules: Flow<List<Schedule>> = scheduleDao.getSelectedSchedules()

    suspend fun addTask(task: Task) = taskDao.insertTask(task)

    suspend fun addTasks(tasks: List<Task>) = taskDao.insertTasks(tasks)

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTaskByName(name: String) {
        taskDao.getTasksByName(name)?.let { taskDao.deleteTask(it) }
    }

    suspend fun deleteTaskById(id: Int) {
        taskDao.getTasksById(id)?.let { taskDao.deleteTask(it) }
    }

    suspend fun deleteAllTasks() = taskDao.deleteAll()

    suspend fun getAllTasks(): List<Task> = taskDao.getAllTasks()

    suspend fun addSchedule(schedule: Schedule, tasks: List<Task>) {
        scheduleDao.insertSchedule(schedule)
        taskDao.insertTasks(tasks)
    }

    suspend fun deleteSchedule(groupName: String) {
        scheduleDao.deleteScheduleByGroup(groupName)
    }

    suspend fun getSchedule(groupName: String): Schedule? {
        return scheduleDao.getSchedule(groupName)
    }

    suspend fun selectSchedule(name:String, value:Boolean) {
        scheduleDao.updateSelected(name, value)
    }
}