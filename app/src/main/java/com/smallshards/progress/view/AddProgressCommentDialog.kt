package com.smallshards.progress.view

import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.smallshards.progress.R
import java.math.BigInteger

class AddProgressCommentDialog(context: Context) : MaterialAlertDialogBuilder(context) {

    private val editView by lazy {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.dialog_add_progress_comment, null)
    }

    var comment: String
        get() {
            val view = editView.findViewById<TextInputEditText>(R.id.comment_inputtext)
            return view.text.toString()
        }
        set(value) {
            val view = editView.findViewById<TextInputEditText>(R.id.comment_inputtext)
            view.text = Editable.Factory().newEditable(value)
        }

    var goal1Reached: Boolean
        get() {
            val view = editView.findViewById<MaterialCheckBox>(R.id.comment_goal1_checkbox)
            return view.isChecked
        }
        set(value) {
            val view = editView.findViewById<MaterialCheckBox>(R.id.comment_goal1_checkbox)
            view.isChecked = value
        }

    var goal2Reached: Boolean
        get() {
            val view = editView.findViewById<MaterialCheckBox>(R.id.comment_goal2_checkbox)
            return view.isChecked
        }
        set(value) {
            val view = editView.findViewById<MaterialCheckBox>(R.id.comment_goal2_checkbox)
            view.isChecked = value
        }

    var goal3Reached: Boolean
        get() {
            val view = editView.findViewById<MaterialCheckBox>(R.id.comment_goal3_checkbox)
            return view.isChecked
        }
        set(value) {
            val view = editView.findViewById<MaterialCheckBox>(R.id.comment_goal3_checkbox)
            view.isChecked = value
        }

    fun setGoalsFromBits(bits: Long) {
        val bigInt = BigInteger.valueOf(bits)
        goal1Reached = (bigInt.testBit(0))
        goal2Reached = (bigInt.testBit(1))
        goal3Reached = (bigInt.testBit(2))
    }

    /**
     * <p>Check if any of the comment fields are checked. Values are will be useful after a call to show</p>
     *
     */
    val isCommentSet: Boolean
        get() = (comment.isNotEmpty() && (goal1Reached || goal2Reached || goal3Reached))

    fun setCommentLabel(value: String): AddProgressCommentDialog {
        val view = editView.findViewById<MaterialTextView>(R.id.comment_title_textview)
        view?.text = value

        return this
    }

    fun setGoal1Label(value: String): AddProgressCommentDialog {
        val view = editView.findViewById<MaterialCheckBox>(R.id.comment_goal1_checkbox)
        view?.text = value

        return this
    }

    fun setGoal2Label(value: String): AddProgressCommentDialog {
        val view = editView.findViewById<MaterialCheckBox>(R.id.comment_goal2_checkbox)
        view?.text = value

        return this
    }

    fun setGoal3Label(value: String): AddProgressCommentDialog {
        val view = editView.findViewById<MaterialCheckBox>(R.id.comment_goal3_checkbox)
        view?.text = value

        return this
    }

    /**
     * check view item controls. if the items do not have a label set the item is removed from the view
     */
    private fun validateAndSetGoalVisibility(view: View) {

        for (viewId in arrayOf(
            R.id.comment_goal1_checkbox,
            R.id.comment_goal2_checkbox,
            R.id.comment_goal3_checkbox
        )) {
            val checkbox = view.findViewById<MaterialCheckBox>(viewId)
            if (checkbox != null) {
                if (checkbox.text.isNullOrEmpty()) checkbox.visibility =
                    View.GONE else checkbox.visibility = View.VISIBLE
            }
        }

    }

    override fun create(): AlertDialog {
        setTitle(context.getString(R.string.add_message_dialog_title))
        setIcon(R.drawable.ic_loyalty_black_24dp)

        validateAndSetGoalVisibility(editView)

        setView(editView)

        return super.create()
    }

}