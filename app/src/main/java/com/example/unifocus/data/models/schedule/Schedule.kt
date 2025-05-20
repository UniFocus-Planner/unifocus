package com.example.unifocus.data.models.schedule

import androidx.room.*
import com.example.unifocus.data.models.task.Task

@Entity(tableName = "schedule")
data class Schedule(
    @PrimaryKey val groupName: String,
    val isSelected: Boolean = false,
)