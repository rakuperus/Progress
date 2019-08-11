package com.smallshards.progress.view

/**
 * <p>This class is used to represent the data points in the actual 2D space.
 * All points are able to animate in 2D and in size</p>
 */
class DataPoint(x: Float, y: Float, val dateTime: Long, val bounce: BounceDirection = BounceDirection.NONE) {

    companion object {
        const val DATA_POINT_SIZE = 14F
        const val ANIM_VELOCITY = 10F
    }

    enum class BounceDirection {
        HORIZONTAL,
        VERTICAL,
        BOTH,
        NONE
    }

    var distanceToCover = 30
    var increaseY = true

    var currentX: Float = x
    var currentY: Float = if (bounce in BounceDirection.VERTICAL..BounceDirection.BOTH) y - distanceToCover else y
    var targetX: Float = x
    var targetY: Float = y

    var currentSize = DATA_POINT_SIZE
    var targetSize = DATA_POINT_SIZE
    var velocity: Float = ANIM_VELOCITY


    var atRest = (bounce == BounceDirection.NONE)

    var detailDataPoints: List<DataPoint> = emptyList()

    fun nextAnimFrame() {
        if (atRest || bounce == BounceDirection.NONE) return

        if (currentY in targetY - 1..targetY + 1 && distanceToCover <= 0) {
            atRest = true
            return
        }

        // TODO: make this speedup or slowdown
        currentY += if (increaseY) velocity else -velocity
        if (!increaseY && currentY < (targetY - distanceToCover)) {
            distanceToCover -= 4
            increaseY = true
        } else if (increaseY && currentY > (targetY + distanceToCover)) {
            distanceToCover -= 4
            increaseY = false
        }

    }
}