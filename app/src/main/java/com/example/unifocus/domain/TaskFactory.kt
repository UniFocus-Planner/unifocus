package com.example.unifocus.domain

import com.example.unifocus.data.models.task.Task
import com.example.unifocus.data.models.task.TaskType
import java.time.LocalDateTime
import java.util.Calendar

class TaskFactory {
    companion object {
        fun createTask(
            name: String,
            deadline: LocalDateTime? = null,
            notificationTime: Calendar? = null,
            taskType: TaskType = TaskType.NONE,
            room: String? = null,
            group: String? = null,
            teacher: String? = null,
            schedule: List<String> = mutableListOf(),
            number: Int = 0,
            selected: Boolean = false,
            weekType: ScheduleItem.WeekType? = null,
            additionalInformation: String? = null
        ): Task {
            return Task(
                name = name,
                deadline = deadline,
                notificationTime = notificationTime,
                taskType = taskType,
                room = room,
                group = group,
                teacher = teacher,
                weekType = weekType,
                number = number,
                selected = selected,
                schedule1 = if(schedule.isNotEmpty()) schedule.get(0) else "",
                schedule2 = if(schedule.isNotEmpty()) schedule.get(1) else "",
                additionalInformation = additionalInformation
            )
        }
    }
}