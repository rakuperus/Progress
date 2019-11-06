package com.smallshards.progress.extension

import android.content.Context
import android.util.DisplayMetrics

/**
 * Convert given dimensions in dp to pixels
 * @param   dp  dimensions in display pixels (dp)
 * @return  dimension in pixels
 */
fun Context.dpToPx(dp: Int): Int =
    dp * this.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT

