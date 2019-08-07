package com.smallshards.progress.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewDebug
import androidx.lifecycle.ViewModelProviders
import com.smallshards.progress.MainActivity
import com.smallshards.progress.R
import com.smallshards.progress.model.progress.Progress
import com.smallshards.progress.viewmodel.ProgressViewModel

class GraphView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        const val TAG_NAME = "GraphView"
    }

    /**
     * <p>
     *     Set or get the progress data that is used to draw the graph.<br/>
     *     After the progress data is set the View is invalidated and redrawn.
     * </p>
     *
     * @return an array of points
     */
    var progressData: List<Progress> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    /**
     * <p>Return the upper limit of this progress graph's range.</p>
     *
     * @return a positive integer
     */
    @ViewDebug.ExportedProperty(category = "range")
    private val minVerticalValue: Int

    /**
     * <p>Return the upper limit of this progress graph's range.</p>
     *
     * @return a positive integer
     */
    @ViewDebug.ExportedProperty(category = "range")
    private val maxVerticalValue: Int

    private val verticalRange: Int
    private val strokeWidth: Float
    private val strokeColor: Int

    init {
        // get the min and max values from the XML attributes
        context.obtainStyledAttributes(attrs, R.styleable.ProgressView, 0, 0).apply {
            try {
                strokeWidth = getDimension(R.styleable.ProgressView_strokeWidth, 4F)
                strokeColor = getColor(R.styleable.ProgressView_strokeColor, 0xFFAAAAAA.toInt())

                val tempMinVerticalValue = getInteger(R.styleable.ProgressView_minProgress, 0)
                val tempMaxVerticalValue = getInteger(R.styleable.ProgressView_maxProgress, 100)

                // validate the min and max value and reset where required
                minVerticalValue =
                    when {
                        tempMinVerticalValue < 0 -> 0
                        tempMinVerticalValue > tempMaxVerticalValue -> tempMaxVerticalValue
                        else -> tempMinVerticalValue
                    }

                maxVerticalValue =
                    when {
                        tempMaxVerticalValue < minVerticalValue -> tempMinVerticalValue
                        else -> tempMaxVerticalValue
                    }
            } finally {
                recycle()
            }
        }


        verticalRange = maxVerticalValue - minVerticalValue

        Log.d(TAG_NAME, "Values range from $minVerticalValue to $maxVerticalValue")
        Log.d(TAG_NAME, "Context is of type ${context.javaClass.simpleName}")
    }

    private lateinit var progressViewModel: ProgressViewModel

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val parentActivity = context as MainActivity
        progressViewModel = ViewModelProviders.of(parentActivity).get(ProgressViewModel::class.java)
        progressViewModel.allProgress.observe(parentActivity, androidx.lifecycle.Observer { progress ->
            progress?.let { progressData = it }
        })

    }

    override fun onDraw(canvas: Canvas?) {

        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = strokeColor
        paint.strokeWidth = strokeWidth
        paint.isAntiAlias = true
        paint.setShadowLayer(4F, 2F, 2F, 0x80000000.toInt())

        if (progressData.isNotEmpty()) {
            val path = Path()
            path.moveTo(getScaledX(0), getScaledY(progressData[0].progressValue.toFloat()))
            for (i in 1 until progressData.size) {
                path.lineTo(getScaledX(i), getScaledY(progressData[i].progressValue.toFloat()))
            }

            canvas?.drawPath(path, paint)
        }
    }

    private fun getScaledY(value: Float): Float {
        val viewHeight = height - (paddingTop + paddingBottom)
        val normalizedValue = when (value) {
            in minVerticalValue.toFloat()..maxVerticalValue.toFloat() -> value
            else -> maxVerticalValue.toFloat()
        }
        val scaledValue = normalizedValue - minVerticalValue

        val scaledY = viewHeight - ((scaledValue / maxVerticalValue.toFloat()) * viewHeight) + paddingTop

        return scaledY
    }

    private fun getScaledX(index: Int): Float {
        val viewWidth = width - (paddingStart + paddingEnd)

        val scaledX = if (progressData.isNotEmpty())
            ((index.toFloat() / (progressData.size - 1).toFloat()) * viewWidth) + paddingStart
        else viewWidth.toFloat()

        return scaledX
    }
}