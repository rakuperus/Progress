package com.smallshards.progress.viewmodel

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smallshards.progress.R
import kotlin.math.pow

@BindingAdapter("app:progressItemTint")
fun progressItemTint(view: ImageView, itemSet: Boolean) {
    view.setColorFilter(
        when (itemSet) {
            true -> view.context.getColor(R.color.primaryColor)
            else -> view.context.getColor(R.color.primaryDisabled)
        }
    )
}

class ProgressItemInfoViewModel : ViewModel() {
    private val _itemInfoSet = MutableLiveData<Boolean>(false)
    /**
     * this observable value indicates if this item holds data that should be recorded
     */
    val itemInfoSet: LiveData<Boolean> = _itemInfoSet

    private val _message = MutableLiveData<String>("")
    val message: LiveData<String> = _message

    fun changeMessage(value: String) {
        _message.value = value
        _itemInfoSet.value = true
    }

    private val _goalBits = MutableLiveData(0L)
    val goalBits: LiveData<Long> = _goalBits

    fun changeGoalBits(goals: Array<Boolean>) {

        var bits = 0L
        goals.forEachIndexed { index, goalSet ->
            if (goalSet) bits = bits or 2F.pow(index + 1).toLong()
        }

        _goalBits.value = bits
        _itemInfoSet.value = (bits != 0L)
    }

    fun clearProgressInfo() {
        _message.value = ""
        _goalBits.value = 0L
        _itemInfoSet.value = false
    }

}