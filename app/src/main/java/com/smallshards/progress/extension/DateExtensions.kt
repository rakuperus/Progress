package com.smallshards.progress.extension

import java.text.SimpleDateFormat
import java.util.*

private const val FULL_DAY_MILLIS = 86400000L

/**
 * <p> helper function used to add a day to a date in format yyyyMMdd </p>
 *
 * @param   days    days to add to the date
 * @returns date
 */
fun Date.addDays(days: Int): Date {
    val c = Calendar.getInstance()

    c.time = this
    c.add(Calendar.DAY_OF_MONTH, days)

    return c.time
}

/**
 * <p>helper function used to determine the number of days between two days</P>
 *
 * @param   date  date to compare to this date
 * @returns number of days between the dates, might be negative it @date is bigger than this
 */
fun Date.daysBetween(date: Date): Long = (this.time - date.time) / FULL_DAY_MILLIS

/**
 * <p>Helper function to format a day in yyyyMMdd format as long. Usefull when working with ranges
 * of dates</p>
 *
 * @returns date as long format in yyyyMMdd, for example 20180324 for 24th of March 2018
 */
val Date.formattedAsLong: Long
    get() = SimpleDateFormat("yyyyMMdd", Locale.forLanguageTag("nl-NL")).format(this).toLong()