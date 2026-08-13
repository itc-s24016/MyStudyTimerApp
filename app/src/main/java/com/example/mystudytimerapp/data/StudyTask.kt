package com.example.mystudytimerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_tasks")
data class StudyTask(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val selectedMinutes: Int = 25,
    val remainingSeconds: Int? = null
)

