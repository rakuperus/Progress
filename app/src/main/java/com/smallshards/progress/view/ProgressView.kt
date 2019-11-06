package com.smallshards.progress.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewDebug
import com.smallshards.progress.R
import com.smallshards.progress.extension.addDays
import com.smallshards.progress.extension.dpToPx
import com.smallshards.progress.extension.getScaledY
import com.smallshards.progress.model.progress.Progress
import com.smallshards.progress.viewmodel.DataPoint
import com.smallshards.progress.viewmodel.ViewDataPointList
import com.smallshards.progress.viewmodel.ViewDataSet
import java.util.*

class ProgressView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        const val CURVE_SMOOTHNESS = 0.15F
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
     * <p>Return wetter a shadow will be visible as part of the stroke.</p>
     *
     * @return a boolean
     */
    @ViewDebug.ExportedProperty(category = "graph")
    private val strokeShadowVisible: Boolean

    /**
     * <p>Return the number of data points to show on the horizontal scale .</p>
     *
     * @return a positive integer
     */
    @ViewDebug.ExportedProperty(category = "graph")
    private val horizontalScale: Int

    private val pathPaint = Paint()
    private val dataPointPaint = Paint()
    private val linePaint = Paint()
    private val pillPaint = Paint()
    private val animPaint = Paint()

    private val path = Path()

    /**
     * <p>Set the progress data that is used to draw the graph.<br/>
     * After the progress data is set the View is invalidated and redrawn.</p>
     *
     * <p>as a side effect of an assignment the view is invalidated, starting a new draw cycle</p>
     *
     * @return an array of points
     */
    private var viewData: ViewDataSet = ViewDataSet(0)
        set(value) {
            field = value

            removeCallbacks(animator)
            post(animator)
        }

    /**
     * <p>the default ProgressView constructor will retrieve all values from the
     * XML resource attributes and initialize internal properties<p>
     * <p>Properties supported by this view
     * <ul>
     *     <li>minProgress -> minimal vertical value</li>
     *     <li>maxProgress -> maximum vertical value</li>
     *     <li>strokeColor -> color of the progress line</li>
     *     <li>strokeWidth -> width of the progress line</li>
     *     <li>strokeWidth -> width of the progress line</li>
     *     <li>strokeShadowVisible -> should a </li>
     *  </ul>
     *  </p>
     */
    init {
        // get the all attributes from the resource
        context.obtainStyledAttributes(attrs, R.styleable.ProgressView, 0, 0).apply {
            try {
                strokeWidth =
                    getDimension(
                        R.styleable.ProgressView_strokeWidth,
                        R.integer.default_stroke_width.toFloat()
                    )
                strokeColor =
                    getColor(
                        R.styleable.ProgressView_strokeColor,
                        resources.getColor(R.color.default_graph_stroke, null)
                    )
                strokeShadowVisible =
                    getBoolean(R.styleable.ProgressView_strokeShadowVisible, false)

                val tempMinVerticalValue =
                    getInteger(R.styleable.ProgressView_minProgress, R.integer.default_vertical_min)
                val tempMaxVerticalValue =
                    getInteger(R.styleable.ProgressView_maxProgress, R.integer.default_vertical_max)

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

                horizontalScale = getInteger(
                    R.styleable.ProgressView_horizontalScale,
                    R.integer.default_horizontal_scale
                )
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
        val shadowColor = Color.LTGRAY

        pathPaint.style = Paint.Style.STROKE
        pathPaint.color = strokeColor
        pathPaint.strokeWidth = strokeWidth
        pathPaint.isAntiAlias = true
        if (strokeShadowVisible) {
            pathPaint.setShadowLayer(4F, 2F, 2F, shadowColor)
        }

        dataPointPaint.style = Paint.Style.STROKE
        dataPointPaint.color = strokeColor
        dataPointPaint.strokeWidth = strokeWidth
        dataPointPaint.isAntiAlias = true
        if (strokeShadowVisible) {
            dataPointPaint.setShadowLayer(4F, 2F, 2F, shadowColor)
        }

        animPaint.style = Paint.Style.STROKE
        animPaint.color = context.getColor(R.color.secondaryColor)
        animPaint.strokeWidth = strokeWidth
        animPaint.isAntiAlias = true

        pillPaint.style = Paint.Style.STROKE
        pillPaint.color = Color.LTGRAY
        pillPaint.strokeWidth = 3F
        pillPaint.isAntiAlias = true

        linePaint.style = Paint.Style.STROKE
        linePaint.color = Color.LTGRAY
        linePaint.strokeWidth = 2F
        linePaint.isAntiAlias = true
    }

    /**
     * <p>Change the data set that is used to render this view. The data set is stored in the private
     * variable viewData. The view will be invalidated after the data has been changed.</p>
     * @param   value   a list of {@link Progress} items retrieved from a datasource
     */
    fun changeProgressDataSet(value: List<Progress>) {
        val startDate = Date().addDays(-horizontalScale)

        viewData = ViewDataSet.convertProgressToViewDataSet(
            value,
            this,
            horizontalScale,
            startDate,
            minVerticalValue,
            maxVerticalValue
        )
    }

    override fun onDraw(canvas: Canvas) {
        drawHorizontalScaleLines(canvas, linePaint)

        if (viewData.isNotEmpty()) {
            drawDataPoints(canvas, dataPointPaint, pillPaint, viewData)
            drawGraph(canvas, pathPaint, viewData)
            drawLastItem(canvas, animPaint, viewData.lastPointAdded)
        }
    }

    /**
     * Draw horizontal scale lines at 25%, 50% and 75% of the canvas
     */
    private fun drawHorizontalScaleLines(canvas: Canvas, paint: Paint) {
        val viewIndent = width * 0.05F
        canvas.drawLine(
            paddingStart.toFloat() + viewIndent,
            height * 0.25F,
            (width - paddingEnd - viewIndent),
            height * 0.25F,
            paint
        )
        canvas.drawLine(
            paddingStart.toFloat(),
            height * 0.50F,
            (width - paddingEnd).toFloat(),
            height * 0.50F,
            paint
        )
        canvas.drawLine(
            paddingStart.toFloat() + viewIndent,
            height * 0.75F,
            (width - paddingEnd - viewIndent),
            height * 0.75F,
            paint
        )
    }

    /**
     * Draw all items in the range starting from rangeStart and ending at rangeEnd from the list
     * points onto the canvas as a Bezier curve.
     *
     * @param canvas        the canvas that will be drawn on
     * @param paint         paint to use for the curve
     * @param points        an list of {@link DataPoint} instances
     * @param smoothness    the flowing factor of the curve
     */
    private fun drawGraph(
        canvas: Canvas,
        paint: Paint,
        points: ViewDataSet,
        smoothness: Float = CURVE_SMOOTHNESS
    ) {
        data class Point(
            var x: Float,
            var y: Float
        )

        // if there is only one point, there is no point to draw the line (pun intended)
        if (points.size <= 1) return

        // get an array of the keys in the points
        val pi = points.map { it.key }

        path.reset()
        path.moveTo(points[pi[0]]!!.x, points[pi[0]]!!.y)
        for (i in 1 until pi.size) {
            val crntPoint = points[pi[i]] ?: continue
            val prevPoint = (if (i - 1 >= 0) points[pi[i - 1]] else points[pi[0]]) ?: continue
            val rightPoint = (if (i + 1 < pi.size) points[pi[i + 1]] else crntPoint) ?: continue
            val leftPoint = (if (i - 2 >= 0) points[pi[i - 2]] else points[pi[0]]) ?: continue

            val leftDiffX = prevPoint.x - leftPoint.x
            val leftDiffY = prevPoint.y - leftPoint.y

            val rightDiffX = rightPoint.x - crntPoint.x
            val rightDiffY = rightPoint.y - crntPoint.y

            val controlLeft = Point(
                prevPoint.x + (smoothness * leftDiffX),
                prevPoint.y + (smoothness * leftDiffY)
            )

            val controlRight = Point(
                crntPoint.x - (smoothness * rightDiffX),
                crntPoint.y - (smoothness * rightDiffY)
            )

            if (prevPoint.y > crntPoint.y) {
                // previous point below current point
                if (controlLeft.y > prevPoint.y) controlLeft.y = prevPoint.y
                if (controlRight.y < crntPoint.x) controlRight.x = crntPoint.x
            } else {
                // previous point above current point
                if (controlLeft.y < prevPoint.y) controlLeft.y = prevPoint.y
                if (controlRight.y > crntPoint.x) controlRight.x = crntPoint.x
            }

            path.cubicTo(
                controlLeft.x, controlLeft.y,
                controlRight.x, controlRight.y,
                crntPoint.x, crntPoint.y
            )
        }

        canvas.drawPath(path, paint)
    }

    private fun drawDataPoints(
        canvas: Canvas,
        paint: Paint,
        pillPaint: Paint,
        points: ViewDataSet
    ) {

        points
            .filter { it.value.x.toInt() in 0..canvas.width }
            .forEach {
                val point = it.value

                drawDataPointPill(canvas, point, pillPaint)

                if (point.goalBits > 0) {
                    drawGoalsReached(canvas, point)

                } else {
                    canvas.drawCircle(point.x, point.y, point.currentSize, paint)
                }
            }
    }

    private fun drawGoalsReached(canvas: Canvas, point: ViewDataPointList) {
        val width = context.dpToPx(24)
        val height = context.dpToPx(24)

        val x = point.x.toInt() - width / 2
        val y = point.y.toInt() - height / 2

        val d = context.resources.getDrawable(R.drawable.ic_star_black_24dp, null)
        d.setBounds(x, y, x + width, y + height)

        val color = when {
            point.goal1Set -> R.color.one_star_color
            point.goal2Set -> R.color.two_star_color
            point.goal3Set -> R.color.three_star_color
            else -> R.color.colorAccent
        }
        d.setColorFilter(
            context.resources.getColor(color, null),
            PorterDuff.Mode.MULTIPLY
        )
        d.draw(canvas)
    }

    private fun drawDataPointPill(canvas: Canvas, point: ViewDataPointList, paint: Paint) {
        when {
            point.items.size == 0 -> { /* skip */
            }
            point.items.size == 1 || point.min == point.max -> {
                canvas.drawCircle(point.x, point.y, point.currentSize + context.dpToPx(4), paint)
            }
            else -> {
                val minY = getScaledY(point.min.toInt(), minVerticalValue, maxVerticalValue)
                val maxY = getScaledY(point.max.toInt(), minVerticalValue, maxVerticalValue)

                val radius = point.currentSize + context.dpToPx(4)

                val oval = RectF()
                oval.set(point.x - radius, minY - radius, point.x + radius, minY + radius)
                canvas.drawArc(oval, 0F, 180F, false, paint)

                oval.set(point.x - radius, maxY - radius, point.x + radius, maxY + radius)
                canvas.drawArc(oval, 180F, 180F, false, paint)

                canvas.drawLine(oval.left, maxY, oval.left, minY, paint)
                canvas.drawLine(oval.right, maxY, oval.right, minY, paint)

                point.items.forEach {
                    canvas.drawCircle(point.x, it.y, context.dpToPx(1).toFloat(), paint)
                }
            }
        }
    }

    private fun drawLastItem(canvas: Canvas, paint: Paint, point: DataPoint?) {
        if (point == null) return

        canvas.drawCircle(point.x, point.y, point.currentSize, paint)
    }

    /**
     * <p>This animator object triggers a redraw until all points are at rest</p>
     */
    private val animator = object : Runnable {
        override fun run() {
            if (animateProgress()) {
                postDelayed(this, 10)
            }

            invalidate()
        }
    }

    /**
     * <p>set the next animation frame and return wetter animation should continue</p>
     *
     * @return  true if animation should continue
     */
    private fun animateProgress(): Boolean = viewData.lastPointAdded?.nextAnimFrame() ?: false
}