package com.example.unifocus.data.models.task
import androidx.room.*

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String?,
    // Нужно изменить тип атрибута deadline c Long? на DateTime?
    val deadline: Long?,
    val taskType: TaskType,
    val additionalInformation: String?
)