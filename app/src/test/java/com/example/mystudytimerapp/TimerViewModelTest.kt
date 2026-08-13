package com.example.mystudytimerapp

import com.example.mystudytimerapp.data.StudyTask
import com.example.mystudytimerapp.data.StudyTaskRepository
import com.example.mystudytimerapp.ui.viewmodel.TimerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: StudyTaskRepository
    private lateinit var viewModel: TimerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val fakeDao = FakeStudyTaskDao()
        repository = StudyTaskRepository(fakeDao)
        viewModel = TimerViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun defaultState_is25Minutes() = runTest {
        val state = viewModel.uiState.value
        assertEquals(25, state.selectedMinutes)
        assertEquals(25 * 60, state.remainingSeconds)
        assertFalse(state.isRunning)
        assertFalse(state.isFinished)
    }

    @Test
    fun selectMinutes_updatesSelectedAndRemainingSeconds() = runTest {
        viewModel.selectMinutes(5)
        val state = viewModel.uiState.value
        assertEquals(5, state.selectedMinutes)
        assertEquals(5 * 60, state.remainingSeconds)
    }

    @Test
    fun startAndPauseTimer_updatesRunningState() = runTest {
        viewModel.selectMinutes(1)
        viewModel.startTimer()

        assertTrue(viewModel.uiState.value.isRunning)

        viewModel.pauseTimer()

        assertFalse(viewModel.uiState.value.isRunning)
    }

    @Test
    fun resetTimer_resetsRemainingTimeToSelectedMinutes() = runTest {
        viewModel.selectMinutes(5)
        viewModel.startTimer()
        testDispatcher.scheduler.advanceTimeBy(3000)

        viewModel.resetTimer()

        val state = viewModel.uiState.value
        assertEquals(5 * 60, state.remainingSeconds)
        assertFalse(state.isRunning)
        assertFalse(state.isFinished)
    }

    @Test
    fun completeTask_updatesTaskCompletionInRepository() = runTest {
        val task = StudyTask(id = 1, title = "Roomの課題", isCompleted = false)
        repository.insert(task)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.initTask(1)
        testDispatcher.scheduler.advanceUntilIdle()

        var navCalled = false
        viewModel.completeTask { navCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(navCalled)
        val updatedTask = repository.getTaskById(1)
        assertTrue(updatedTask?.isCompleted == true)
    }

    @Test
    fun pauseTimer_persistsProgressAndRestoresOnReinit() = runTest {
        val task = StudyTask(id = 1, title = "Kotlinの復習", isCompleted = false)
        repository.insert(task)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.initTask(1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectMinutes(5)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.startTimer()
        testDispatcher.scheduler.advanceTimeBy(10000)
        testDispatcher.scheduler.runCurrent()
        viewModel.pauseTimer()
        testDispatcher.scheduler.advanceUntilIdle()

        val savedTask = repository.getTaskById(1)
        assertEquals(5, savedTask?.selectedMinutes)
        assertEquals(290, savedTask?.remainingSeconds)

        val newViewModel = TimerViewModel(repository)
        newViewModel.initTask(1)
        testDispatcher.scheduler.advanceUntilIdle()

        val newState = newViewModel.uiState.value
        assertEquals(5, newState.selectedMinutes)
        assertEquals(290, newState.remainingSeconds)
        assertFalse(newState.isRunning)
    }
}
