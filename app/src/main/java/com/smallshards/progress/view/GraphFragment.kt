package com.smallshards.progress.view

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smallshards.progress.R
import com.smallshards.progress.model.progress.Progress
import com.smallshards.progress.viewmodel.ProgressViewModel
import kotlinx.android.synthetic.main.fragment_graph.*
import kotlinx.android.synthetic.main.progress_item.view.*


class GraphFragment : Fragment() {

    private lateinit var progressViewModel: ProgressViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_graph, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ProgressAdapter(this.activity as Activity)
        progressList.adapter = adapter
        progressList.layoutManager = LinearLayoutManager(this.activity)

        progressViewModel = ViewModelProviders.of(this.activity!!).get(ProgressViewModel::class.java)
        progressViewModel.allProgress.observe(this, androidx.lifecycle.Observer { progress ->
            progress?.let { adapter.setProgressData(it) }
        })

    }

    class ProgressAdapter internal constructor(context: Context) :
        RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder>() {

        private val inflator: LayoutInflater = LayoutInflater.from(context)
        private var progressData = emptyList<Progress>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
            return ProgressViewHolder(
                inflator.inflate(R.layout.progress_item, parent, false)
            )
        }

        override fun getItemCount() = progressData.size

        override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
            val currentItem = progressData[position]
            holder.idTextView.text = currentItem.id.toString()
            holder.dateTimeTextView.text = "${currentItem.date} ${currentItem.time}"
            holder.progressTextView.text = currentItem.progressValue.toString()
        }

        internal fun setProgressData(newProgressData: List<Progress>) {
            this.progressData = newProgressData
            notifyDataSetChanged()
        }

        inner class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val idTextView: TextView = itemView.progressId
            val dateTimeTextView: TextView = itemView.dateTime
            val progressTextView: TextView = itemView.progressValue
        }
    }
}
