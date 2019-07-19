package com.smallshards.progress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import com.smallshards.progress.model.*

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
}