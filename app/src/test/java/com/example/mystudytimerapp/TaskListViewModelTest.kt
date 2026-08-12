package com.example.mystudytimerapp

import com.example.mystudytimerapp.data.StudyTask
import com.example.mystudytimerapp.data.StudyTaskDao
import com.example.mystudytimerapp.data.StudyTaskRepository
import com.example.mystudytimerapp.ui.viewmodel.TaskFilter
import com.example.mystudytimerapp.ui.viewmodel.TaskListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FakeStudyTaskDao : StudyTaskDao {
    private val tasksMap = mutableMapOf<Int, StudyTask>()
    private val tasksFlow = MutableStateFlow<List<StudyTask>>(emptyList())
    private var idCounter = 1

    private fun emit() {
        tasksFlow.value = tasksMap.values.sortedByDescending { it.id }
    }

    override suspend fun insert(task: StudyTask) {
        val id = if (task.id == 0) idCounter++ else task.id
        tasksMap[id] = task.copy(id = id)
        emit()
    }

    override suspend fun update(task: StudyTask) {
        tasksMap[task.id] = task
        emit()
    }

    override suspend fun delete(task: StudyTask) {
        tasksMap.remove(task.id)
        emit()
    }

    override suspend fun deleteCompleted() {
        tasksMap.entries.removeIf { it.value.isCompleted }
        emit()
    }

    override fun getAll(): Flow<List<StudyTask>> = tasksFlow

    override fun getByIdFlow(id: Int): Flow<StudyTask?> {
        return MutableStateFlow(tasksMap[id])
    }

    override suspend fun getById(id: Int): StudyTask? {
        return tasksMap[id]
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: StudyTaskRepository
    private lateinit var viewModel: TaskListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val fakeDao = FakeStudyTaskDao()
        repository = StudyTaskRepository(fakeDao)
        viewModel = TaskListViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addTask_emptyString_showsErrorMessage() = runTest {
        viewModel.onInputTextChanged("   ")
        viewModel.addTask()

        assertEquals("学習内容を入力してください", viewModel.errorMessage.value)
    }

    @Test
    fun addTask_validString_addsTaskAndClearsInput() = runTest {
        viewModel.onInputTextChanged("Kotlinの復習")
        viewModel.addTask()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.inputText.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun deleteTask_showsConfirmationDialog_andDeletes() = runTest {
        val task = StudyTask(id = 1, title = "Composeの復習")
        repository.insert(task)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.requestDeleteTask(task)
        assertEquals(task, viewModel.taskToDelete.value)

        viewModel.confirmDeleteTask()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.taskToDelete.value)
    }

    @Test
    fun setFilter_filtersTasksCorrectly() = runTest {
        repository.insert(StudyTask(id = 1, title = "Task 1", isCompleted = false))
        repository.insert(StudyTask(id = 2, title = "Task 2", isCompleted = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setFilter(TaskFilter.COMPLETED)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(TaskFilter.COMPLETED, viewModel.currentFilter.value)
    }
}
