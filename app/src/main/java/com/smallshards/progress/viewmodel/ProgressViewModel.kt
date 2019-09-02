package com.smallshards.progress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.smallshards.progress.model.progress.Progress
import com.smallshards.progress.model.progress.ProgressDatabase
import com.smallshards.progress.model.progress.ProgressRepository
import kotlinx.coroutines.launch

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    /*
        Stored items
     */
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

    /*
        New items
     */

    private val _message = MutableLiveData("Some default message")
    val progressMessage: LiveData<String> = _message

    fun changeMessage(value: String) {
        _message.value = value
    }

    private val _goalBits = MutableLiveData(0L)
    val goalBits: LiveData<Long> = _goalBits

    fun changeGoalBits(goal1: Boolean, goal2: Boolean, goal3: Boolean) {
        var bits = 0L
        if (goal1) bits = bits or 1
        if (goal2) bits = bits or 2
        if (goal3) bits = bits or 4

        _goalBits.value = bits
    }
}
