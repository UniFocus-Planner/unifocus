package com.example.unifocus.data.models.task
import androidx.room.*
import com.example.unifocus.domain.ScheduleItem
import java.time.LocalDateTime

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String?,
    val deadline: LocalDateTime?,
    val taskType: TaskType,
    val room: String?,
    val group: String?,
    val schedule: String?,
    val teacher: String?,
    val weekType: ScheduleItem.WeekType?,
    val number: Int?,
    var selected: Boolean = false,
    val additionalInformation: String?
)