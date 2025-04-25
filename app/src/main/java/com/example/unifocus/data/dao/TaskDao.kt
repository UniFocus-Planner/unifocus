package com.example.unifocus.data.dao

import androidx.room.*
import com.example.unifocus.data.models.task.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Delete
    suspend fun deleteTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE name = :name LIMIT 1")
    suspend fun getTasksByName(name: String): Task?

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTasksById(id: Int): Task?

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    @Query("SELECT * FROM tasks WHERE taskType = :type")
    fun getTasksByType(type: String): Flow<List<Task>>
}