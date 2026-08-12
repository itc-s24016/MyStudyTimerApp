package com.example.mystudytimerapp.ui.viewmodel

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mystudytimerapp.data.StudyTask
import com.example.mystudytimerapp.data.StudyTaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TimerUiState(
    val taskId: Int = 0,
    val taskTitle: String = "",
    val task: StudyTask? = null,
    val selectedMinutes: Int = 25,
    val remainingSeconds: Int = 25 * 60,
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
                    // Only update task info if not already loaded or matching
                    if (currentState.taskId != taskId) {
                        val seconds = currentState.selectedMinutes * 60
                        currentState.copy(
                            taskId = task.id,
                            taskTitle = task.title,
                            task = task,
                            remainingSeconds = seconds,
                            isRunning = false,
                            isFinished = false,
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
        _uiState.update {
            it.copy(
                selectedMinutes = minutes,
                remainingSeconds = minutes * 60,
                isFinished = false,
                soundPlayed = false
            )
        }
    }

    fun startTimer() {
        if (_uiState.value.remainingSeconds <= 0) return
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = true) }

        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && _uiState.value.isRunning) {
                delay(1000L)
                _uiState.update { state ->
                    val nextSeconds = (state.remainingSeconds - 1).coerceAtLeast(0)
                    if (nextSeconds == 0) {
                        state.copy(
                            remainingSeconds = 0,
                            isRunning = false,
                            isFinished = true
                        )
                    } else {
                        state.copy(remainingSeconds = nextSeconds)
                    }
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun resetTimer() {
        timerJob?.cancel()
        val resetSeconds = _uiState.value.selectedMinutes * 60
        _uiState.update {
            it.copy(
                remainingSeconds = resetSeconds,
                isRunning = false,
                isFinished = false,
                soundPlayed = false
            )
        }
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
                        completedAt = System.currentTimeMillis()
                    )
                )
            } else if (_uiState.value.taskId > 0) {
                val fetched = repository.getTaskById(_uiState.value.taskId)
                if (fetched != null) {
                    repository.update(
                        fetched.copy(
                            isCompleted = true,
                            completedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
            onCompleteNav()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
