package com.smallshards.progress.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewDebug
import com.smallshards.progress.R
import com.smallshards.progress.model.progress.Progress

class ProgressView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        const val TAG_NAME = "ProgressView"
        const val CURVE_SMOOTHENESS = 0.15F
    }

    /**
     * <p>This animator object triggers a redraw until all points are at rest</p>
     */
    private val animator = object : Runnable {
        override fun run() {
            var scheduleNewFrame = false

            progressData.forEach { point ->
                point.nextAnimFrame()
                if (!point.atRest) scheduleNewFrame = true
            }

            if (scheduleNewFrame) {
                postDelayed(this, 10)
            }

            invalidate()
        }
    }

    /**
     * <p>Set the progress data that is used to draw the graph.<br/>
     * After the progress data is set the View is invalidated and redrawn.</p>
     *
     * <p>as a side effect of an assignment the view is invalidated, starting a new draw cycle</p>
     *
     * @return an array of points
     */
    private var progressData: List<DataPoint> = emptyList()
        set(value) {
            field = value

            removeCallbacks(animator)
            post(animator)
        }

    /**
     * <p>Transform the progress data to 2D data points on the given view<p>
     *
     *  @return an array of 2D data points
     */
    private fun mapProgressToDataPoints(
        value: List<Progress>,
        scaleX: Int,
        minY: Int,
        maxY: Int,
        view: View
    ): List<DataPoint> {
        val limit = value.size
        return value
            .sortedBy { it.dateTime }
            .mapIndexed { idx, it ->
                DataPoint(
                    view.getScaledX(idx, limit, scaleX, 14),
                    view.getScaledY(it.progressValue.toInt(), minY, maxY, 14),
                    it.goalsReached,
                    it.dateTime,
                    if (idx == limit - 1) DataPoint.BounceDirection.VERTICAL else DataPoint.BounceDirection.NONE
                )
            }
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

    /**
     * <p>Return the stroke width of the graph.</p>
     *
     * @return a positive float
     */
    @ViewDebug.ExportedProperty(category = "graph")
    private val strokeWidth: Float

    /**
     * <p>Return the color of the graph as an integer.</p>
     *
     * @return a positive integer
     */
    @ViewDebug.ExportedProperty(category = "graph")
    private val strokeColor: Int

    /**
     * <p>Return the number of data points to show on the horizontal scale .</p>
     *
     * @return a positive integer
     */
    @ViewDebug.ExportedProperty(category = "graph")
    private val horizontalScale: Int

    private val verticalRange: Int

    private val pathPaint = Paint()
    private val dataPointPaint = Paint()
    private val linePaint = Paint()
    private val path = Path()

    private val viewContext = context

    init {
        // get the min and max values from the XML attributes
        context.obtainStyledAttributes(attrs, R.styleable.ProgressView, 0, 0).apply {
            try {
                strokeWidth = getDimension(R.styleable.ProgressView_strokeWidth, 4F)
                strokeColor =
                    getColor(R.styleable.ProgressView_strokeColor, android.graphics.Color.argb(0xff, 0xaa, 0xaa, 0xaa))

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

                horizontalScale = getInteger(R.styleable.ProgressView_horizontalScale, 8)

                verticalRange = maxVerticalValue - minVerticalValue
            } finally {
                this.recycle()
            }
        }

        initDrawObjects()
    }

    /**
     * <p>Initialize the drawing objects. These are initialized once, so not to trigger garabage collect during Draw
     */
    private fun initDrawObjects() {
        val shadowColor = android.graphics.Color.LTGRAY

        pathPaint.style = Paint.Style.STROKE
        pathPaint.color = strokeColor
        pathPaint.strokeWidth = strokeWidth
        pathPaint.isAntiAlias = true
        pathPaint.setShadowLayer(4F, 2F, 2F, shadowColor)

        dataPointPaint.style = Paint.Style.FILL_AND_STROKE
        dataPointPaint.color = strokeColor
        dataPointPaint.strokeWidth = strokeWidth
        dataPointPaint.isAntiAlias = true
        dataPointPaint.setShadowLayer(4F, 2F, 2F, shadowColor)

        linePaint.style = Paint.Style.STROKE
        linePaint.color = android.graphics.Color.LTGRAY
        linePaint.strokeWidth = 2F
        linePaint.isAntiAlias = true
    }

    fun changeProgressData(value: List<Progress>) {
        progressData = mapProgressToDataPoints(
            value,
            horizontalScale,
            minVerticalValue,
            maxVerticalValue,
            this
        )
    }

    override fun onDraw(canvas: Canvas) {
        drawMedium(canvas)

        if (progressData.isNotEmpty()) {
            val startFromIdx = if (progressData.size > horizontalScale) progressData.size - horizontalScale else 0

            drawGraph(canvas, progressData, startFromIdx, progressData.size)
            drawDataPoints(canvas, progressData, startFromIdx, progressData.size)

        }
    }

    private fun drawMedium(canvas: Canvas) {
        canvas.drawLine(paddingStart.toFloat(), height / 2F, (width - paddingEnd).toFloat(), height / 2F, linePaint)
    }

    private data class Point(
        val x: Float,
        val y: Float
    )

    private fun drawGraph(canvas: Canvas, points: List<DataPoint>, rangeStart: Int, rangeEnd: Int) {
        val rs = if (rangeStart > 0) rangeStart - 1 else rangeStart

        path.reset()

        path.moveTo(points[rs].currentX, points[rs].currentY)
        for (idx in rs + 1 until rangeEnd) {
            val crntPoint = points[idx]
            val prevPoint = if (idx - 1 >= 0) points[idx - 1] else points[0]
            val rightPoint = if (idx + 1 < rangeEnd) points[idx + 1] else crntPoint
            val leftPoint = if (idx - 2 >= 0) points[idx - 2] else points[0]

            val leftDiffX = prevPoint.currentX - leftPoint.currentX
            val leftDiffY = prevPoint.currentY - leftPoint.currentY

            val rightDiffX = rightPoint.currentX - crntPoint.currentX
            val rightDiffY = rightPoint.currentY - crntPoint.currentY

            val controlLeft = Point(
                prevPoint.currentX + (CURVE_SMOOTHENESS * leftDiffX),
                prevPoint.currentY + (CURVE_SMOOTHENESS * leftDiffY)
            )

            val controlRight = Point(
                crntPoint.currentX - (CURVE_SMOOTHENESS * rightDiffX),
                crntPoint.currentY - (CURVE_SMOOTHENESS * rightDiffY)
            )

            path.cubicTo(
                controlLeft.x, controlLeft.y,
                controlRight.x, controlRight.y,
                crntPoint.currentX, crntPoint.currentY
            )
        }

        canvas.drawPath(path, pathPaint)
    }

    private fun drawDataPoints(canvas: Canvas, points: List<DataPoint>, rangeStart: Int, rangeEnd: Int) {

        points.subList(rangeStart, rangeEnd).forEach {

            if (it.goalBits > 0) {
                val width = dpToPx(24, viewContext)
                val height = dpToPx(24, viewContext)

                val x = it.currentX.toInt() - width / 2
                val y = it.currentY.toInt() - height / 2

                val d = viewContext.resources.getDrawable(R.drawable.ic_star_black_24dp, null)
                d.setBounds(x, y, x + width, y + height)

                val color = when {
                    it.goal1Set -> R.color.one_star_color
                    it.goal2Set -> R.color.two_star_color
                    it.goal3Set -> R.color.three_star_color
                    else -> R.color.colorAccent
                }
                d.setColorFilter(
                    viewContext.resources.getColor(color, null),
                    PorterDuff.Mode.MULTIPLY
                )
                d.draw(canvas)

            } else {
                canvas.drawCircle(it.currentX, it.currentY, it.currentSize, dataPointPaint)
            }
        }


    }

    private fun dpToPx(dp: Int, context: Context): Int =
        dp * context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT

}

/**
 * <p>scale a value between a min and max on the current view</p>
 *
 * @return an scaled float value.
 */
private fun View.getScaledY(value: Int, minValue: Int, maxValue: Int, viewMargin: Int = 0): Float {
    val viewHeight = height - (paddingTop + paddingBottom + viewMargin * 2)
    val normalizedValue = when {
        value > maxValue -> maxValue
        value < minValue -> minValue
        else -> value
    }
    val scaledValue = normalizedValue - minValue

    return viewHeight - ((scaledValue.toFloat() / maxValue.toFloat()) * viewHeight) + paddingTop + viewMargin
}

/**
 * <p>scale an index that is in a list with given limit onto the given scale</p>
 *
 * @return an scaled float value or -1000F if the index is outside the range
 */
private fun View.getScaledX(index: Int, limit: Int, scale: Int, viewMargin: Int = 0): Float {
    val rangeStart = if (limit > scale) limit - scale else 0
    val rangeEnd = scale - 1

    var x = -1001F

    // only determine values for visible value, including one outside of the view
    if (index in rangeStart - 1..limit) {
        val viewWidth = width - (paddingStart + paddingEnd + viewMargin * 2)

        if (limit > 0) {
            x = (((index - rangeStart).toFloat() / (rangeEnd).toFloat()) * viewWidth) + paddingStart + viewMargin
        }
    }

    return x
}