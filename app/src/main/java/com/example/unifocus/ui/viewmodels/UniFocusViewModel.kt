package com.example.unifocus.ui.viewmodels

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
        description: String? = null,
        deadline: LocalDateTime? = null,
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
            description = description,
            deadline = deadline,
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
            val tasks = repository.getTasksBySchedule(schedule.groupName)
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

    fun scheduleNotification(
        context: Context?,
        targetTime: Calendar?,
        notificationId: Int,
        channelId: String,
        title: String,
        text: String
    ) {
        if (targetTime == null) {
            return
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notificationId", notificationId)
            putExtra("channelId", channelId)
            putExtra("title", title)
            putExtra("text", text)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Android 6.0+
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
    }
}
