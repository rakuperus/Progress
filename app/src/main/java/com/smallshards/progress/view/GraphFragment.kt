package com.smallshards.progress.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.smallshards.progress.R
import com.smallshards.progress.viewmodel.ProgressViewModel
import kotlinx.android.synthetic.main.fragment_graph.*

class GraphFragment : Fragment() {

    private val progressViewModel by lazy { ViewModelProvider(this).get(ProgressViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_graph, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // on an update of the data update the progress view
        progressViewModel.allProgress.observe(this, androidx.lifecycle.Observer { progress ->
            progress?.let { progressView.changeProgressDataSet(progress) }
        })

    }
}
