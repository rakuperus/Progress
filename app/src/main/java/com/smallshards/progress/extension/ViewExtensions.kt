package com.smallshards.progress.extension

import android.view.View

/**
 * <p>scale a value between a min and max on the current view</p>
 *
 * @return an scaled float value.
 */
fun View.getScaledY(value: Int, minValue: Int, maxValue: Int, viewMargin: Int = 0): Float {
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
 * <p>Scale an index that is in a integer list from 0 to range. If the index out side of the range
 * it is scaled to default values of -1000 or width+1000</p>
 *
 * @param   index   index to scale within the view
 * @param   range   maximum range of the integer list
 * @param   margin  horizontal margin to apply on top of the padding already set in the view
 *
 * @return a scaled float in the views visible range
 */
fun View.getScaledX(index: Int, range: Int, margin: Int = 0): Float {

    return when {
        index in 0..range -> {
            val viewWidth = width - (paddingStart + paddingEnd + margin * 2)

            ((index.toFloat() / range.toFloat()) * viewWidth) + paddingStart + margin
        }
        index < 0 -> -1000F
        index > range -> width + 1000F
        else -> 0F
    }
}

