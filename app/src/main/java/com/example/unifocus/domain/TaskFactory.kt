package com.example.unifocus.domain

import com.example.unifocus.data.models.task.Task
import com.example.unifocus.data.models.task.TaskType
import java.time.LocalDateTime

class TaskFactory {
    companion object {
        fun createTask(
            name: String,
            description: String? = null,
            deadline: LocalDateTime? = null,
            taskType: TaskType = TaskType.NONE,
            room: String? = null,
            group: String? = null,
            teacher: String? = null,
            schedule: List<String>? = null,
            number: Int = 0,
            selected: Boolean = false,
            weekType: ScheduleItem.WeekType? = null,
            additionalInformation: String? = null
        ): Task {
            return Task(
                name = name,
                description = description,
                deadline = deadline,
                taskType = taskType,
                room = room,
                group = group,
                teacher = teacher,
                weekType = weekType,
                number = number,
                selected = selected,
                schedule1 = schedule?.get(0),
                schedule2 = schedule?.get(1),
                additionalInformation = additionalInformation
            )
        }
    }
}