package com.smallshards.progress.view

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.smallshards.progress.R


class MainLayoutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_layout, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.main_app_menu, menu)

}
