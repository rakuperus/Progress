package com.smallshards.progress

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smallshards.progress.extension.addDays
import com.smallshards.progress.model.progress.Progress
import com.smallshards.progress.viewmodel.ProgressViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    private val progressViewModel by lazy { ViewModelProvider(this).get(ProgressViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(main_toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_app_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_all_progress_menu_item -> {
                deleteAllProgress()
            }
            R.id.add_magic_progress_menu_item -> {
                addMagicProgress()
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun addMagicProgress(): Boolean {
        val rnd = Random(Date().time)

        for (i in 0..rnd.nextInt(10)) {
            progressViewModel.insert(
                Progress(
                    Date().addDays(-rnd.nextInt(10)).time,
                    rnd.nextInt(100).toLong()
                )
            )
        }

        return true
    }

    fun deleteAllProgress(): Boolean {

        MaterialAlertDialogBuilder(this)
            .setMessage("All stored progress will be deleted. Are you sure you want to continue?")
            .setPositiveButton("Ok") { _, _ ->
                progressViewModel.deleteAll()

            }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()

        return true
    }


}
