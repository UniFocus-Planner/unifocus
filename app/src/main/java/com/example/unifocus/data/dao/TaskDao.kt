package com.example.unifocus.data.dao

import androidx.room.*
import com.example.unifocus.data.models.task.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE taskType = :type ORDER BY deadline ASC")
    fun getTasksByType(type: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id IN (:taskIds)")
    fun getTasksByIds(taskIds: List<Int>): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTasks(tasks: List<Task>): List<Long>

    @Delete
    fun deleteTask(task: Task)
}