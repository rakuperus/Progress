package com.smallshards.progress.model.progress

import androidx.annotation.WorkerThread

class ProgressRepository(private val progressDao: ProgressDao) {

    val allProgress = progressDao.getAllProgress()

    @WorkerThread
    suspend fun insert(item: Progress) {
        progressDao.insert(item)
    }

    @WorkerThread
    suspend fun deleteAll() = progressDao.deleteAll()

}