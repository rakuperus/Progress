package com.smallshards.progress.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.smallshards.progress.R
import com.smallshards.progress.model.progress.Progress
import com.smallshards.progress.viewmodel.ProgressViewModel
import kotlinx.android.synthetic.main.fragment_add_progress.*
import java.util.*

class AddProgressFragment : Fragment() {

    private lateinit var progressViewModel: ProgressViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_add_progress, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSeekbarLabel(progressIndicatorSeekBar.progress)
        setFlyoutVisibility(View.INVISIBLE)

        addDataPointButton.setOnClickListener {
            addProgressClicked()
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

        progressViewModel = ViewModelProviders.of(this.activity!!).get(ProgressViewModel::class.java)
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
                progressIndicatorSeekBar.progress + 1L
            )
        )
    }


}
