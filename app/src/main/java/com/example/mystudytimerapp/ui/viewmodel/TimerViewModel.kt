package com.example.mystudytimerapp.ui.viewmodel

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mystudytimerapp.data.StudyTask
import com.example.mystudytimerapp.data.StudyTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TimerUiState(
    val taskId: Int = 0,
    val taskTitle: String = "",
    val task: StudyTask? = null,
    val selectedMinutes: Int = 25,
    val remainingSeconds: Int = 25 * 60,
    val savedRemainingSeconds: Int? = null,
    val isResumeMode: Boolean = false,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val soundPlayed: Boolean = false
)

class TimerViewModel(private val repository: StudyTaskRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun initTask(taskId: Int) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            if (task != null) {
                _uiState.update { currentState ->
                    if (currentState.taskId != taskId) {
                        val selMin = task.selectedMinutes
                        val savedRem = task.remainingSeconds
                        val hasSavedProgress = savedRem != null && savedRem < (selMin * 60) && savedRem > 0
                        val isResume = hasSavedProgress
                        val remSec = if (isResume && savedRem != null) savedRem else (selMin * 60)
                        val finished = remSec <= 0

                        currentState.copy(
                            taskId = task.id,
                            taskTitle = task.title,
                            task = task,
                            selectedMinutes = selMin,
                            remainingSeconds = remSec,
                            savedRemainingSeconds = if (hasSavedProgress) savedRem else null,
                            isResumeMode = isResume,
                            isRunning = false,
                            isFinished = finished,
                            soundPlayed = false
                        )
                    } else {
                        currentState.copy(
                            taskTitle = task.title,
                            task = task
                        )
                    }
                }
            }
        }
    }

    fun selectMinutes(minutes: Int) {
        if (_uiState.value.isRunning) return
        val newSeconds = minutes * 60
        _uiState.update {
            it.copy(
                selectedMinutes = minutes,
                remainingSeconds = newSeconds,
                isResumeMode = false,
                isFinished = false,
                soundPlayed = false
            )
        }
    }

    fun selectResumeMode() {
        if (_uiState.value.isRunning) return
        val saved = _uiState.value.savedRemainingSeconds ?: return
        _uiState.update {
            it.copy(
                remainingSeconds = saved,
                isResumeMode = true,
                isFinished = false,
                soundPlayed = false
            )
        }
    }

    fun startTimer() {
        if (_uiState.value.remainingSeconds <= 0) return
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = true, isResumeMode = true) }

        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && _uiState.value.isRunning) {
                delay(1000L)
                _uiState.update { state ->
                    val nextSeconds = (state.remainingSeconds - 1).coerceAtLeast(0)
                    if (nextSeconds == 0) {
                        state.copy(
                            remainingSeconds = 0,
                            savedRemainingSeconds = 0,
                            isRunning = false,
                            isFinished = true
                        )
                    } else {
                        state.copy(
                            remainingSeconds = nextSeconds,
                            savedRemainingSeconds = nextSeconds
                        )
                    }
                }
            }
            if (_uiState.value.remainingSeconds == 0) {
                saveProgress()
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update {
            val updatedSaved = if (it.isResumeMode || it.remainingSeconds < it.selectedMinutes * 60) {
                it.remainingSeconds
            } else {
                it.savedRemainingSeconds
            }
            it.copy(
                isRunning = false,
                savedRemainingSeconds = updatedSaved
            )
        }
        saveProgress()
    }

    fun resetTimer() {
        timerJob?.cancel()
        val resetSeconds = _uiState.value.selectedMinutes * 60
        _uiState.update {
            it.copy(
                remainingSeconds = resetSeconds,
                savedRemainingSeconds = null,
                isResumeMode = false,
                isRunning = false,
                isFinished = false,
                soundPlayed = false
            )
        }
        saveProgress()
    }

    fun playSoundEffect() {
        if (_uiState.value.soundPlayed) return
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 600)
            _uiState.update { it.copy(soundPlayed = true) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun completeTask(onCompleteNav: () -> Unit) {
        timerJob?.cancel()
        val currentTask = _uiState.value.task
        viewModelScope.launch {
            if (currentTask != null) {
                repository.update(
                    currentTask.copy(
                        isCompleted = true,
                        completedAt = System.currentTimeMillis(),
                        remainingSeconds = null
                    )
                )
            } else if (_uiState.value.taskId > 0) {
                val fetched = repository.getTaskById(_uiState.value.taskId)
                if (fetched != null) {
                    repository.update(
                        fetched.copy(
                            isCompleted = true,
                            completedAt = System.currentTimeMillis(),
                            remainingSeconds = null
                        )
                    )
                }
            }
            onCompleteNav()
        }
    }

    fun saveProgress() {
        val currentTask = _uiState.value.task ?: return
        val state = _uiState.value
        val remToSave = if (state.isResumeMode || (state.remainingSeconds < state.selectedMinutes * 60 && state.remainingSeconds > 0)) {
            state.remainingSeconds
        } else {
            state.savedRemainingSeconds
        }
        viewModelScope.launch {
            val updatedTask = currentTask.copy(
                selectedMinutes = state.selectedMinutes,
                remainingSeconds = remToSave
            )
            repository.update(updatedTask)
            _uiState.update { it.copy(task = updatedTask) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
