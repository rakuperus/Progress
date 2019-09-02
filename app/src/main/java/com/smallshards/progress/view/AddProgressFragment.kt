package com.smallshards.progress.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.smallshards.progress.R
import com.smallshards.progress.databinding.FragmentAddProgressBinding
import com.smallshards.progress.model.progress.Progress
import com.smallshards.progress.viewmodel.ProgressViewModel
import kotlinx.android.synthetic.main.fragment_add_progress.*
import java.util.*

class AddProgressFragment : Fragment() {

    private val progressViewModel by lazy {
        ViewModelProviders.of(this.activity!!).get(ProgressViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentAddProgressBinding>(
            inflater,
            R.layout.fragment_add_progress,
            container,
            false
        )
        binding.viewmodel = progressViewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSeekbarLabel(progressIndicatorSeekBar.progress)
        setFlyoutVisibility(View.INVISIBLE)

        addDataPointButton.setOnClickListener {
            addProgressClicked()
        }

        addMessageButton.setOnClickListener {
            addMessageToProgress()
        }

        progressIndicatorSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setSeekbarLabel(progress)

                val constraintSet = ConstraintSet()
                constraintSet.clone(progressLayout)
                val biasedValue = progress / progressIndicatorSeekBar.max.toFloat()
                constraintSet.setHorizontalBias(progressIndicatorExtendedTick.id, biasedValue)
                constraintSet.applyTo(progressLayout)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                setFlyoutVisibility(View.VISIBLE)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                setFlyoutVisibility(View.INVISIBLE)
            }
        })
    }

    private fun setFlyoutVisibility(visibility: Int) {
        progressIndicatorExtendedTick.visibility = visibility
        progressIndicatorFlyoutTick.visibility = visibility
        progressIndicatorText.visibility = visibility
    }

    private fun setSeekbarLabel(progress: Int) {
        progressIndicatorText.text = (progress / 10).toString()
    }

    private fun addProgressClicked() {
        val currentDateTime = Calendar.getInstance().time

        progressViewModel.insert(
            Progress(
                currentDateTime.time,
                progressIndicatorSeekBar.progress.toLong(),
                progressViewModel.progressMessage.value ?: ""
            )
        )
    }

    private fun addMessageToProgress() {

        val builder = AddProgressCommentDialog(context!!)
            .setGoal1Label("Workout completed")
            .setGoal2Label("Run completed")

        // retrieve values from previous runs
        builder.comment = progressViewModel.progressMessage.value ?: ""
        builder.setGoalsFromBits(progressViewModel.goalBits.value ?: 0L)

        builder.setPositiveButton(R.string.add_message_confirm) { _, _ ->
            // update the view model
            progressViewModel.changeMessage(builder.comment)
            progressViewModel.changeGoalBits(
                builder.goal1Reached,
                builder.goal2Reached,
                builder.goal3Reached
            )
        }
        builder.setNegativeButton(R.string.add_message_dismiss) { _, _ ->
            Log.d("AddProgressFragment", "Dialog results ignored")
        }

        builder.create()
        builder.show()
    }
}