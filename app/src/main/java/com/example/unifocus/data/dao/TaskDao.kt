package com.example.unifocus.data.dao

import androidx.room.*
import com.example.unifocus.data.models.task.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE taskType = :type ORDER BY deadline ASC")
    fun getTasksByType(type: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id IN (:taskIds)")
    suspend fun getTasksByIds(taskIds: List<Int>): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>): List<Long>

    @Delete
    suspend fun deleteTask(task: Task)
}