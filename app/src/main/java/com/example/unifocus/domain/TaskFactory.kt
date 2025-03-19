package com.example.unifocus.domain

import com.example.unifocus.data.models.task.Task
import com.example.unifocus.data.models.task.TaskType

class TaskFactory {
    companion object {
        fun createTask(
            name: String,
            description: String? = null,
            deadline: Long? = null,
            taskType: TaskType = TaskType.NONE,
            additionalInformation: String? = null
        ): Task {
            return Task(
                name = name,
                description = description,
                deadline = deadline ?: System.currentTimeMillis(),
                taskType = taskType,
                additionalInformation = additionalInformation
            )
        }
    }
}