package com.example.unifocus.data.dao

import androidx.room.*
import com.example.unifocus.data.models.schedule.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule WHERE groupName = :group")
    fun getSchedule(group: String): Schedule?

    @Query("SELECT * FROM schedule")
    fun getSchedules(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedule WHERE isSelected = :value")
    fun getSelectedSchedules(value:Boolean = true): Flow<List<Schedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSchedule(schedule: Schedule)

    @Query("DELETE FROM schedule WHERE groupName = :group")
    fun deleteScheduleByGroup(group: String)

    @Query("UPDATE schedule SET isSelected = :selected WHERE groupName = :group")
    fun updateSelected(group: String, selected: Boolean)
}