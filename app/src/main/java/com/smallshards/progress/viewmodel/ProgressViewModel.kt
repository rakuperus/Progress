package com.smallshards.progress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.smallshards.progress.model.progress.Progress
import com.smallshards.progress.model.progress.ProgressDatabase
import com.smallshards.progress.model.progress.ProgressRepository
import kotlinx.coroutines.launch

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val repository : ProgressRepository
    val allProgress: LiveData<List<Progress>>

    init {
        val progressDao = ProgressDatabase.getDatabase(application, viewModelScope).progressDao()
        repository = ProgressRepository(progressDao)
        allProgress = repository.allProgress
    }

    fun insert(item: Progress) = viewModelScope.launch {
        repository.insert(item)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}