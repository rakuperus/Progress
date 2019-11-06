package com.smallshards.progress.viewmodel

import android.view.View
import com.smallshards.progress.extension.*
import com.smallshards.progress.model.progress.Progress
import java.math.BigInteger
import java.util.*

/**
 * <p>This class is used to represent the data points in the actual 2D space.
 * All points are able to animate in 2D and in size</p>
 */
class DataPoint(val x: Float, val y: Float, val goalBits: Long) {

    var atRest = false
    var animatePoint = false

    var currentSize = 10F
    var maxSize = 50F
    var minSize = 5F
    var increase = 6F

    fun nextAnimFrame(): Boolean {
        if (atRest) return false

        if (currentSize < minSize) {
            atRest = true
            return false
        }

        currentSize += increase
        if (currentSize > maxSize) increase = -increase

        return true
    }
}

/**
 * <p>List of data points for a given date<p>
 */
class ViewDataPointList(val date: Long, val x: Float) {

    val items = mutableListOf<DataPoint>()

    var average = 0L
    var min = -1L
    var max = -1L
    var goalBits = 0L
    var count = 0
    var sum = 0L

    private var sumY = 0F
    var y: Float = 0F

    val goal1Set: Boolean
        get() = (BigInteger.valueOf(goalBits).testBit(0))

    val goal2Set: Boolean
        get() = (BigInteger.valueOf(goalBits).testBit(1))

    val goal3Set: Boolean
        get() = (BigInteger.valueOf(goalBits).testBit(2))

    val currentSize = 24F

    fun add(value: Long, pointY: Float, bits: Long): DataPoint {
        val point = DataPoint(x, pointY, bits)
        items.add(point)

        if (min < 0 || value < min) min = value
        if (max < 0 || value > max) max = value

        count++
        sum += value
        average = sum / count

        goalBits = goalBits or bits

        sumY += pointY
        y = sumY / count

        return point
    }
}


/**
 * A sorted map of DataPoints indexed and sorted by date
 *
 * @param   zoomLevel   zoomLevel determines the maximum number of items in this map, the actual
 *                      size might be lower
 */
class ViewDataSet(private val zoomLevel: Int) : TreeMap<Long, ViewDataPointList>() {

    var lastPointAdded: DataPoint? = null
        private set

    companion object {

        /**
         * <p>Convert a list of {@link Progress} items into a new instance of {@link ViewDataSet}
         * within the given view</p>
         * <p>Depending on the zoomLevel the ViewDataSet will contain more or less items<p>
         */
        fun convertProgressToViewDataSet(
            source: List<Progress>,
            view: View,
            zoomLevel: Int,
            startDate: Date,
            minVerticalProgress: Int,
            maxVerticalProgress: Int
        ): ViewDataSet =

            when (zoomLevel) {
                in 0..1 -> convertProgressToHours(zoomLevel)
                in 2..8 -> convertProgressToDays(
                    source,
                    view,
                    zoomLevel,
                    startDate,
                    minVerticalProgress,
                    maxVerticalProgress
                )
                in 9..31 -> convertProgressToWeeks(zoomLevel)
                else -> convertProgressToMonths(zoomLevel)
            }

        private fun convertProgressToHours(zoomLevel: Int): ViewDataSet = ViewDataSet(zoomLevel)
        private fun convertProgressToMonths(zoomLevel: Int): ViewDataSet = ViewDataSet(zoomLevel)
        private fun convertProgressToWeeks(zoomLevel: Int): ViewDataSet = ViewDataSet(zoomLevel)

        private fun convertProgressToDays(
            source: List<Progress>,
            view: View,
            zoomLevel: Int,
            startDate: Date,
            minVerticalProgress: Int,
            maxVerticalProgress: Int
        ): ViewDataSet {
            val v = ViewDataSet(zoomLevel)

            // determine horizontal bounding box
            val endDate = startDate.addDays(zoomLevel)
            val minDate = startDate.addDays(-1).formattedAsLong
            val maxDate = startDate.addDays(zoomLevel + 1).formattedAsLong

            var lastPoint: DataPoint? = null

            source
                .filter { it.date in minDate..maxDate }
                .forEach {
                    val dateViewPoints = v[it.date] ?: run {
                        val itemDate = Date(it.dateTime)
                        val horizontalIndex = zoomLevel - endDate.daysBetween(itemDate).toInt()
                        val pointX = view.getScaledX(horizontalIndex, zoomLevel)

                        ViewDataPointList(it.date, pointX)
                    }


                    val pointY = view.getScaledY(
                        it.progressValue.toInt(),
                        minVerticalProgress,
                        maxVerticalProgress
                    )

                    lastPoint = dateViewPoints.add(
                        it.progressValue, pointY, it.goalsReached
                    )

                    v[it.date] = dateViewPoints
                }

            // the last point from the list is the one added last, the view could animate on that
            lastPoint?.animatePoint = true
            v.lastPointAdded = lastPoint

            return v
        }
    }
}