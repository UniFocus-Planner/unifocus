package com.example.unifocus.models.task

class TaskFactory {
    fun createTask(
        name: String,
        type: TaskType,
        hasPriority: Boolean
    ) : Task {
        return Task(
            name = name,
            type = type,
            priority = hasPriority,
            comment = ""
        )
    }
}