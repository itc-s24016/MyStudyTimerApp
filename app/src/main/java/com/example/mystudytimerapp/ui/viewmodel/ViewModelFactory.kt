package com.example.mystudytimerapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mystudytimerapp.data.StudyTaskRepository

class ViewModelFactory(private val repository: StudyTaskRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
            return TaskListViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            return TimerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
