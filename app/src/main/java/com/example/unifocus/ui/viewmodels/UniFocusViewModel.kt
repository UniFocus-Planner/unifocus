package com.example.unifocus.ui.viewmodels

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.unifocus.data.models.schedule.Schedule
import com.example.unifocus.data.models.task.Task
import com.example.unifocus.data.models.task.TaskType
import com.example.unifocus.data.repository.UniFocusRepository
import com.example.unifocus.domain.NotificationReceiver
import com.example.unifocus.domain.ScheduleFactory
import com.example.unifocus.domain.ScheduleItem
import com.example.unifocus.domain.TaskFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Calendar

class UniFocusViewModel(private val repository: UniFocusRepository) : ViewModel() {

    val classTasks: LiveData<List<Task>> = repository.classTasks.asLiveData()
    val schedules: LiveData<List<Schedule>> = repository.schedules.asLiveData()
    val selectedSchedules: LiveData<List<Schedule>> = repository.selectedSchedules.asLiveData()
    val todaySelectedTasks: LiveData<List<Task>> = repository.todayTasks.asLiveData()

    fun toggleTaskSelection(taskId: Int, selected: Boolean) {
        viewModelScope.launch {
            repository.updateTaskSelection(taskId, selected)
        }
    }

    fun createTask(
        name: String,
        deadline: LocalDateTime? = null,
        notificationTime: Calendar? = null,
        notificationId: Int? = null,
        taskType: TaskType = TaskType.NONE,
        room: String? = null,
        teacher: String? = null,
        number: Int = 0,
        selected: Boolean = false,
        schedule: List<String> = mutableListOf(),
        group: String = "",
        weekType: ScheduleItem.WeekType? = null,
        additionalInformation: String? = null
    ): Task {
        val task = TaskFactory.createTask( name = name,
            deadline = deadline,
            notificationTime = notificationTime,
            taskType = taskType,
            room = room,
            teacher = teacher,
            weekType = weekType,
            number = number,
            selected = selected,
            schedule = schedule,
            group = group,
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

    suspend fun addTaskAndGetId(task: Task): Int {
        return repository.addTaskAndGetId(task)
    }

    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
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

            Log.d("SELECTING", "SELECTING")
            var tasks = repository.getTasksBySchedule(schedule.groupName)
            if (tasks.isEmpty()) tasks = repository.getTasksByGroup(schedule.groupName)

            tasks.forEach {
                Log.d("TASK", tasks.toString())
            }
            tasks.forEach {
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

    fun getTotalTaskCount(): Int {
        var count = 0
        viewModelScope.launch(Dispatchers.IO) {
            count = repository.getTotalTaskCount()
        }
        return count
    }

    suspend fun getAllTasks(): List<Task> {
        return repository.getAllTasks()
    }

    fun logTaskInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val tasks = getAllTasks()
            Log.d("Task", "TASKS:")
            for(task in tasks) {
                Log.d("Task", "${task.toString()}")
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleNotification(
        context: Context?,
        targetTime: Calendar?,
        id: Int,
        channelId: String,
        title: String,
        text: String
    ) {
        if (targetTime == null) {
            return
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notificationId", id)
            putExtra("channelId", channelId)
            putExtra("title", title)
            putExtra("text", text)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                targetTime.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                targetTime.timeInMillis,
                pendingIntent
            )
        }

        Log.d("Notifications", "Scheduled notification with ID ${id} on ${targetTime.toString()}")
    }

    suspend fun deleteNotification(
        context: Context?,
        id: Int,
        channelId: String,
    ) {
        if (context == null) return

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notificationId", id)
            putExtra("channelId", channelId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()

            Log.d("Notifications", "Deleted notification with ID $id")
        }
    }

    suspend fun rescheduleNotification(
        context: Context?,
        targetTime: Calendar?,
        id: Int,
        channelId: String,
        title: String,
        text: String
    ) {
        Log.d("Notifications", "Rescheduling notification with ID ${id}")

        deleteNotification(context, id, channelId)
        scheduleNotification(context, targetTime, id, channelId, title, text)
    }

    fun deleteObsoleteNotifications(context: Context?) {
        viewModelScope.launch(Dispatchers.IO) {
            val channelId = "Unifocus"
            val tasks = getAllTasks()

            if (tasks.isEmpty()) {
                for (id in 1..9999) {
                    deleteNotification(context, id, channelId)
                }
                return@launch
            }

            val validIdsWithNotification = tasks
                .filter { it.notificationTime != null }
                .map { it.id }
                .toSet()

            val maxId = validIdsWithNotification.maxOrNull() ?: 0
            val allPossibleIds = (1..maxId).toSet()
            val obsoleteIds = allPossibleIds - validIdsWithNotification

            obsoleteIds.forEach { id ->
                deleteNotification(context, id, channelId)
            }
        }
    }
}

