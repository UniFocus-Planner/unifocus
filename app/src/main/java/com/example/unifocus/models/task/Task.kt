package com.example.unifocus.models.task

data class Task(
    private var name: String,
    private var type: TaskType,
    private var comment: String,
    private var priority: Boolean
) {

}