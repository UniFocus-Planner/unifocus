package com.example.unifocus.data.repository

import com.example.unifocus.data.database.Converters
import com.example.unifocus.data.database.UniFocusDatabase
import com.example.unifocus.data.models.schedule.Schedule
import com.example.unifocus.data.models.task.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class UniFocusRepository(private val database: UniFocusDatabase) {
    private val taskDao = database.taskDao()
    private val scheduleDao = database.scheduleDao()

    // Отбор задач с типом CLASS
    val classTasks: Flow<List<Task>> = taskDao.getTasksByType("CLASS")
    val todayTasks: Flow<List<Task>> = taskDao.getTodaySelectedTasks(
        startOfDay = Converters().dateToTimestamp(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0))!!,
        endOfDay = Converters().dateToTimestamp(LocalDateTime.now().withHour(23).withMinute(59).withSecond(59))!!
    )

    val schedules: Flow<List<Schedule>> = scheduleDao.getSchedules()
    val selectedSchedules: Flow<List<Schedule>> = scheduleDao.getSelectedSchedules()
    val selectedTasks: Flow<List<Task>> = taskDao.getSelectedTasks()

    suspend fun addTask(task: Task) = taskDao.insertTask(task)

    suspend fun addTaskAndGetId(task: Task): Int {
        return taskDao.insertAndGetId(task).toInt()
    }

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

    suspend fun getTotalTaskCount(): Int = taskDao.countAllTasks()

    suspend fun addSchedule(schedule: Schedule) {
        scheduleDao.insertSchedule(schedule)
    }

    suspend fun updateTaskSelection(taskId: Int, selected: Boolean) {
        database.taskDao().updateTaskSelected(taskId, selected)
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

    suspend fun selectTask(name:String, value:Boolean) {
        taskDao.updateSelected(name, value)
    }

    suspend fun getTasksBySchedule(schedule:String) : List<Task> {
        return taskDao.getTasksBySchedule(schedule)
    }

    suspend fun getTodaySelectedTasks(start: Long, end: Long): Flow<List<Task>>{
        return taskDao.getTodaySelectedTasks(start, end)
    }
}