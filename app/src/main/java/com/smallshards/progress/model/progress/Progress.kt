package com.smallshards.progress.model.progress

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "progress")
data class Progress(
    /**
     * <p>the moment the progress was recorded</p>
     */
    @ColumnInfo(name = "timeMillis")
    val dateTime : Long,

    /**
     * <p>A value indicating the users progress, this value can be plotted<p>
     */
    @ColumnInfo(name = "val")
    val progressValue: Long,

    /**
     * <p>Comments are added by the user on a progress, they are informational only</p>
     */
    @ColumnInfo(name = "comment")
    val comment: String = "",

    /**
     * <p>Goals reached is a set of bits for each goal reached, so bit 1 is goal #1 reached, bit 2 is goal #2, and so
     * on</p>
     */
    @ColumnInfo(name = "goals_reached")
    val goalsReached: Long = 0
) {
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0

    @ColumnInfo(name = "date")
    var date: Long

    @ColumnInfo(name = "time")
    var time: String

    fun isGoalSet(goalNumber: Int): Boolean {
        return (BigInteger.valueOf(goalsReached).testBit(goalNumber - 1))
    }

    init {
        val dt = Date(dateTime)

        val dateFormatter = SimpleDateFormat("yyyyMMdd", Locale.forLanguageTag("nl-NL"))
        date = dateFormatter.format(dt).toLong()

        val timeFormatter = SimpleDateFormat("HH:mm", Locale.forLanguageTag("nl-NL"))
        time = timeFormatter.format(dt)
    }
}