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
import androidx.lifecycle.ViewModelProvider
import com.smallshards.progress.R
import com.smallshards.progress.databinding.FragmentAddProgressBinding
import com.smallshards.progress.model.progress.Progress
import com.smallshards.progress.viewmodel.ProgressItemInfoViewModel
import com.smallshards.progress.viewmodel.ProgressViewModel
import kotlinx.android.synthetic.main.fragment_add_progress.*
import java.util.*

/**
 * This fragment is is used to add a progress value to the database. It consists of a seek bar
 * that shows the actual value (depicted in the layout with colors) and a button to add the value.
 * There is an additional button that will show a dialog used to store additional data.
 */
class AddProgressFragment : Fragment() {

    private val itemInfoViewModel by lazy { ViewModelProvider(this).get(ProgressItemInfoViewModel::class.java) }
    private val progressViewModel by lazy { ViewModelProvider(this).get(ProgressViewModel::class.java) }

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
        binding.lifecycleOwner = this
        binding.iteminfo = itemInfoViewModel

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
            addProgressInfoClicked()
        }

        /**
         * With this handler any change in the seek bar will show a fly out with the actual value.
         * The fly out is constrained in the layout to the location of the seek bar tracker button
         * and will move with it while the user drags the tracker.
         */
        progressIndicatorSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setSeekbarLabel(progress)

                // after each change the constrain layout is recalculated, this way the fly out will move with the value
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

    /**
     * Handler for the add progress button click event. The handler will insert a new value
     * in the progress database
     */
    private fun addProgressClicked() {
        val currentDateTime = Calendar.getInstance().time

        progressViewModel.insert(
            Progress(
                currentDateTime.time,
                progressIndicatorSeekBar.progress.toLong(),
                itemInfoViewModel.message.value ?: "",
                itemInfoViewModel.goalBits.value ?: 0L
            )
        )

        itemInfoViewModel.clearProgressInfo()
    }

    /**
     * Open a dialog showing the progress information screen
     */
    private fun addProgressInfoClicked() {

        val builder = AddProgressCommentDialog(context!!)
            .setGoalLabels(
                arrayOf(
                    getString(R.string.goal1_label),
                    getString(R.string.goal2_label),
                    getString(R.string.goal3_label)
                )
            )

        // retrieve values from previous runs
        builder.comment = itemInfoViewModel.message.value ?: ""
        builder.setGoalsFromBits(itemInfoViewModel.goalBits.value ?: 0L)

        /* positive button clicked handler
         */
        builder.setPositiveButton(R.string.add_message_confirm) { _, _ ->
            // update the view model
            itemInfoViewModel.changeMessage(builder.comment)
            itemInfoViewModel.changeGoalBits(
                arrayOf(
                    builder.goal1Reached,
                    builder.goal2Reached,
                    builder.goal3Reached
                )
            )

        }

        /*  negative button clicked handler
         */
        builder.setNegativeButton(R.string.add_message_dismiss) { _, _ ->
            Log.d("AddProgressFragment", "Dialog results ignored")
        }

        builder.create()
        builder.show()
    }
}