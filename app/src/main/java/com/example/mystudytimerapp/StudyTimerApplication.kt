package com.example.mystudytimerapp

import android.app.Application
import com.example.mystudytimerapp.data.AppDatabase
import com.example.mystudytimerapp.data.StudyTaskRepository

class StudyTimerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { StudyTaskRepository(database.studyTaskDao()) }
}
