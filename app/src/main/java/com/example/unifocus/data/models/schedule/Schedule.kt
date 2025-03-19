package com.example.unifocus.data.models.schedule

import androidx.room.*
import com.example.unifocus.data.models.task.Task

@Entity(tableName = "schedule")
data class Schedule(
    @PrimaryKey val groupName: String,
    @Ignore val tasks: MutableList<Task> = mutableListOf()
) {
    constructor(groupName: String) : this(groupName, mutableListOf())
}