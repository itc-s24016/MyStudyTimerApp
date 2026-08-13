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
    val currentSort: TaskSortOption = TaskSortOption.CREATED_DESC,
    val sortDirection: TaskSortDirection = TaskSortDirection.ASCENDING,
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

    private val _currentSort = MutableStateFlow(TaskSortOption.CREATED_DESC)
    val currentSort: StateFlow<TaskSortOption> = _currentSort.asStateFlow()

    private val _sortDirection = MutableStateFlow(TaskSortDirection.ASCENDING)
    val sortDirection: StateFlow<TaskSortDirection> = _sortDirection.asStateFlow()

    private val _taskToDelete = MutableStateFlow<StudyTask?>(null)
    val taskToDelete: StateFlow<StudyTask?> = _taskToDelete.asStateFlow()

    val uiState: StateFlow<TaskListUiState> = combine(
        repository.allTasks,
        _currentFilter,
        _currentSort,
        _sortDirection,
        _inputText,
        _errorMessage,
        _taskToDelete
    ) { flows ->
        val tasks = flows[0] as List<StudyTask>
        val filter = flows[1] as TaskFilter
        val sort = flows[2] as TaskSortOption
        val direction = flows[3] as TaskSortDirection
        val input = flows[4] as String
        val error = flows[5] as String?
        val taskToDelete = flows[6] as StudyTask?

        val completedCount = tasks.count { it.isCompleted }
        val filtered = when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.INCOMPLETE -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
        }

        val sorted = when (sort) {
            TaskSortOption.CREATED_DESC -> {
                if (direction == TaskSortDirection.ASCENDING) filtered.sortedBy { it.id }
                else filtered.sortedByDescending { it.id }
            }
            TaskSortOption.REMAINING_TIME -> {
                if (direction == TaskSortDirection.ASCENDING)
                    filtered.sortedBy { it.remainingSeconds ?: (it.selectedMinutes * 60) }
                else
                    filtered.sortedByDescending { it.remainingSeconds ?: (it.selectedMinutes * 60) }
            }
            TaskSortOption.NAME -> {
                if (direction == TaskSortDirection.ASCENDING) filtered.sortedBy { it.title }
                else filtered.sortedByDescending { it.title }
            }
        }

        TaskListUiState(
            tasks = tasks,
            filteredTasks = sorted,
            totalCount = tasks.size,
            completedCount = completedCount,
            currentFilter = filter,
            currentSort = sort,
            sortDirection = direction,
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

    fun setSort(sort: TaskSortOption) {
        _currentSort.value = sort
    }

    fun toggleSortDirection() {
        _sortDirection.value = if (_sortDirection.value == TaskSortDirection.ASCENDING) {
            TaskSortDirection.DESCENDING
        } else {
            TaskSortDirection.ASCENDING
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
