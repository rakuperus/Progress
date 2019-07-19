package com.smallshards.progress

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.smallshards.progress.model.Progress
import com.smallshards.progress.viewmodel.ProgressViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.progress_item.view.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var progressViewModel : ProgressViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seekBar.setOnSeekBarChangeListener (object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBarValue.text = "${progress+1}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        val adapter = ProgressAdapter(this)
        progressList.adapter = adapter
        progressList.layoutManager = LinearLayoutManager(this)

        progressViewModel = ViewModelProviders.of(this).get(ProgressViewModel::class.java)
        progressViewModel.allProgress.observe(this, androidx.lifecycle.Observer { progress ->
            progress?.let { adapter.setProgressData( it )}
        })
    }

    fun addProgressClicked(view: View) {
        val currentDateTime = Calendar.getInstance().time

        progressViewModel.insert(Progress(12, currentDateTime.time, seekBar.progress+1L))

        Snackbar.make(mainView, "Added progress at ${currentDateTime.time} with value ${seekBar.progress+1}", Snackbar.LENGTH_SHORT).show()
    }

    class ProgressAdapter internal constructor(context: Context) : RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder>() {

        private val inflator : LayoutInflater = LayoutInflater.from(context)
        private var progressData = emptyList<Progress>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
            return ProgressViewHolder(
                inflator.inflate(R.layout.progress_item, parent, false)
            )
        }

        override fun getItemCount() = progressData.size

        override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
            val currentItem = progressData[position]
            holder.idTextView.text = "${currentItem.id}"
            holder.dateTimeTextView.text = "${currentItem.dateTime}"
            holder.progressTextView.text = "${currentItem.progressValue}"
        }

        internal fun setProgressData(newProgressData: List<Progress>) {
            this.progressData = newProgressData
            notifyDataSetChanged()
        }

        inner class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val idTextView = itemView.progressId
            val dateTimeTextView = itemView.dateTime
            val progressTextView = itemView.progressValue
        }
    }

}
