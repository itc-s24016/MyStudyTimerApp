package com.example.mystudytimerapp.data

import kotlinx.coroutines.flow.Flow

class StudyTaskRepository(private val dao: StudyTaskDao) {
    val allTasks: Flow<List<StudyTask>> = dao.getAll()

    suspend fun insert(task: StudyTask) {
        dao.insert(task)
    }

    suspend fun update(task: StudyTask) {
        dao.update(task)
    }

    suspend fun delete(task: StudyTask) {
        dao.delete(task)
    }

    suspend fun deleteTasks(tasks: List<StudyTask>) {
        dao.deleteTasks(tasks)
    }

    suspend fun deleteCompleted() {
        dao.deleteCompleted()
    }

    fun getTaskByIdFlow(id: Int): Flow<StudyTask?> {
        return dao.getByIdFlow(id)
    }

    suspend fun getTaskById(id: Int): StudyTask? {
        return dao.getById(id)
    }
}
