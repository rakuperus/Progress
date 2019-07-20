package com.smallshards.progress.model.progress

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "progress")
data class Progress(
    @ColumnInfo(name = "timeMillis")
    val dateTime : Long,

    @ColumnInfo(name = "val")
    val progressValue : Long
) {
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0

    @ColumnInfo(name = "date")
    var date: Long

    @ColumnInfo(name = "time")
    var time: String

    init {
        val dt = Date(dateTime)

        val dateFormatter = SimpleDateFormat("yyyyMMdd", Locale.forLanguageTag("nl-NL"))
        date = dateFormatter.format(dt).toLong()

        val timeFormatter = SimpleDateFormat("HH:mm", Locale.forLanguageTag("nl-NL"))
        time = timeFormatter.format(dt)
    }
}