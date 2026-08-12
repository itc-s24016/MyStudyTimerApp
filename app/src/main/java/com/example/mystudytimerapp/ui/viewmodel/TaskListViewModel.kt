package com.example.mystudytimerapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mystudytimerapp.data.StudyTask
import com.example.mystudytimerapp.data.StudyTaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskListUiState(
    val tasks: List<StudyTask> = emptyList(),
    val filteredTasks: List<StudyTask> = emptyList(),
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val currentFilter: TaskFilter = TaskFilter.ALL,
    val inputText: String = "",
    val errorMessage: String? = null,
    val taskToDelete: StudyTask? = null
)

class TaskListViewModel(private val repository: StudyTaskRepository) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentFilter = MutableStateFlow(TaskFilter.ALL)
    val currentFilter: StateFlow<TaskFilter> = _currentFilter.asStateFlow()

    private val _taskToDelete = MutableStateFlow<StudyTask?>(null)
    val taskToDelete: StateFlow<StudyTask?> = _taskToDelete.asStateFlow()

    val uiState: StateFlow<TaskListUiState> = combine(
        repository.allTasks,
        _currentFilter,
        _inputText,
        _errorMessage,
        _taskToDelete
    ) { tasks, filter, input, error, taskToDelete ->
        val completedCount = tasks.count { it.isCompleted }
        val filtered = when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.INCOMPLETE -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
        }
        TaskListUiState(
            tasks = tasks,
            filteredTasks = filtered,
            totalCount = tasks.size,
            completedCount = completedCount,
            currentFilter = filter,
            inputText = input,
            errorMessage = error,
            taskToDelete = taskToDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskListUiState()
    )

    fun onInputTextChanged(text: String) {
        _inputText.value = text
        if (text.isNotBlank() && _errorMessage.value != null) {
            _errorMessage.value = null
        }
    }

    fun addTask() {
        val title = _inputText.value.trim()
        if (title.isEmpty()) {
            _errorMessage.value = "学習内容を入力してください"
            return
        }
        viewModelScope.launch {
            repository.insert(StudyTask(title = title))
            _inputText.value = ""
            _errorMessage.value = null
        }
    }

    fun toggleTaskCompletion(task: StudyTask) {
        viewModelScope.launch {
            val newCompletion = !task.isCompleted
            val completedTime = if (newCompletion) System.currentTimeMillis() else null
            repository.update(task.copy(isCompleted = newCompletion, completedAt = completedTime))
        }
    }

    fun requestDeleteTask(task: StudyTask) {
        _taskToDelete.value = task
    }

    fun dismissDeleteDialog() {
        _taskToDelete.value = null
    }

    fun confirmDeleteTask() {
        val task = _taskToDelete.value ?: return
        viewModelScope.launch {
            repository.delete(task)
            _taskToDelete.value = null
        }
    }

    fun deleteCompletedTasks() {
        viewModelScope.launch {
            repository.deleteCompleted()
        }
    }

    fun setFilter(filter: TaskFilter) {
        _currentFilter.value = filter
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
