package com.example.unifocus.data.dao

import androidx.room.*
import com.example.unifocus.data.models.schedule.Schedule

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule WHERE groupName = :group")
    fun getSchedule(group: String): Schedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSchedule(schedule: Schedule)

    @Query("DELETE FROM schedule WHERE groupName = :group")
    fun deleteScheduleByGroup(group: String)
}