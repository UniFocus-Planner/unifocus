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

    fun addTask(task: Task) {
        taskDao.insertTask(task)
    }

    fun addSchedule(schedule: Schedule, tasks: List<Task>) {
        scheduleDao.insertSchedule(schedule)
        taskDao.insertTasks(tasks)
    }

    fun getSchedule(groupName: String): Schedule? {
        return scheduleDao.getSchedule(groupName)
    }

    fun deleteSchedule(groupName: String) {
        scheduleDao.deleteScheduleByGroup(groupName)
    }
}