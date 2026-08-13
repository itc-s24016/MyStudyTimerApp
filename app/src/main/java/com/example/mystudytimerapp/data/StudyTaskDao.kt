package com.example.mystudytimerapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: StudyTask)

    @Update
    suspend fun update(task: StudyTask)

    @Delete
    suspend fun delete(task: StudyTask)

    @Delete
    suspend fun deleteTasks(tasks: List<StudyTask>)

    @Query("DELETE FROM study_tasks WHERE isCompleted = 1")
    suspend fun deleteCompleted()

    @Query("SELECT * FROM study_tasks ORDER BY id DESC")
    fun getAll(): Flow<List<StudyTask>>

    @Query("SELECT * FROM study_tasks WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<StudyTask?>

    @Query("SELECT * FROM study_tasks WHERE id = :id")
    suspend fun getById(id: Int): StudyTask?
}
