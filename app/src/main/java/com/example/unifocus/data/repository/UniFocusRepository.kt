package com.example.unifocus.data.repository

import com.example.unifocus.data.database.UniFocusDatabase
import com.example.unifocus.data.models.schedule.Schedule
import com.example.unifocus.data.models.task.Task
import kotlinx.coroutines.flow.Flow

class UniFocusRepository(
    private val database: UniFocusDatabase
) {
    private val taskDao = database.taskDao()
    private val scheduleDao = database.scheduleDao()

    val classTasks: Flow<List<Task>> = taskDao.getTasksByType("CLASS")

    suspend fun addTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun addSchedule(schedule: Schedule, tasks: List<Task>) {
        schedule.tasks.addAll(tasks)
        scheduleDao.insertSchedule(schedule)
        taskDao.insertTasks(tasks)
    }

    suspend fun getSchedule(groupName: String): Schedule? {
        return scheduleDao.getSchedule(groupName)
    }

    suspend fun deleteSchedule(groupName: String) {
        scheduleDao.deleteScheduleByGroup(groupName)
    }
}