package com.example.unifocus.data.database

import android.content.Context
import androidx.room.*
import com.example.unifocus.data.models.schedule.Schedule
import com.example.unifocus.data.dao.ScheduleDao
import com.example.unifocus.data.models.task.Task
import com.example.unifocus.data.dao.TaskDao

@Database(entities = [Task::class, Schedule::class], version = 40)
@TypeConverters(Converters::class)
abstract class UniFocusDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: UniFocusDatabase? = null

        fun getDatabase(context: Context): UniFocusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UniFocusDatabase::class.java,
                    "unifocus_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}