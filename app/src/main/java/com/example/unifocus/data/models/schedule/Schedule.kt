package com.example.unifocus.data.models.schedule

import androidx.room.*
import com.example.unifocus.data.models.task.Task

@Entity(tableName = "schedule")
data class Schedule(
    @PrimaryKey val groupName: String,
    val isSelected: Boolean = false,
    @Ignore val tasks: MutableList<Task> = mutableListOf()
) {
    constructor(groupName: String, isSelected: Boolean = false) : this(groupName, isSelected, mutableListOf())
}