package com.example.unifocus.data.dao

import androidx.room.*
import com.example.unifocus.data.models.schedule.Schedule
import com.example.unifocus.data.models.task.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

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

    @Query("UPDATE tasks SET selected = :selected WHERE name = :name")
    fun updateSelected(name: String, selected: Boolean)

    @Query("SELECT * FROM tasks WHERE deadline = :date OR (:date IS NULL AND deadline IS NULL)")
    fun getTasksByDate(date: LocalDateTime?): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE schedule = :schedule")
    fun getTasksBySchedule(schedule: String): List<Task>

    @Query("""
        SELECT * FROM tasks 
        WHERE 
            deadline BETWEEN :startOfDay AND :endOfDay 
            AND selected = 1
    """)
    fun getTodaySelectedTasks(
        startOfDay: Long,
        endOfDay: Long
    ): Flow<List<Task>>

    @Query("UPDATE tasks SET selected = :selected WHERE id = :taskId")
    suspend fun updateTaskSelected(taskId: Int, selected: Boolean)

    @Query("SELECT * FROM tasks WHERE selected = :value")
    fun getSelectedTasks(value:Boolean = true): Flow<List<Task>>

    /*
    *
    * Здесь должен быть SQL-запрос на получение списка задач по выбранной дате
    *
    * Нужно заменить тип Long на DateTime
    *
    * @Query("SELECT * FROM tasks WHERE deadline = :date OR (:date IS NULL AND deadline IS NULL)")
    * fun getTasksByDate(date: Long?): Flow<List<Task>>
    *
    * */
}