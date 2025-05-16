package com.example.unifocus.data.models.task
import androidx.room.*
import com.example.unifocus.domain.ScheduleItem
import java.time.LocalDateTime
import java.util.Calendar

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String?,
    val deadline: LocalDateTime?,
    val notificationTime: Calendar?,
    val taskType: TaskType,
    val room: String?,
    val group: String?,
    val schedule1: String?,
    val schedule2: String?,
    val teacher: String?,
    val weekType: ScheduleItem.WeekType?,
    val number: Int?,
    var selected: Boolean = false,
    val additionalInformation: String?
)