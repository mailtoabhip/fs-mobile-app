package com.delhivery.orion.utils

import android.support.annotation.IntRange
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    /**
     * Convert date to calendar, ie., [year], [month] and [dayOfMonth]
     *
     * @return Calendar with specified date
     */
    fun calendarFromDate(year: Int, @IntRange(from = 1, to = 12) month: Int, @IntRange(from = 1, to = 31) dayOfMonth: Int): Calendar {
        return Calendar.getInstance().let {
            it[Calendar.YEAR] = year
            it[Calendar.MONTH] = month
            it[Calendar.DAY_OF_MONTH] = dayOfMonth
            return@let it
        }
    }

    /**
     * Format Date provided to format specified
     *
     * @param date [Date] to be formatted
     * @param format [String] format
     *
     * @return formatted Date
     */
    fun formatDate(date: Date, format: String): String {
        return try {
            SimpleDateFormat(format, Locale.getDefault()).format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Parse date from formatted date string using format specified
     *
     * @param date formatted [String] date
     * @param format [String] format
     *
     * @return [Date] parsed date, if parse error is thrown then current Date is returned
     */
    fun parseDate(date: String, format: String): Date {
        return try {
            SimpleDateFormat(format, Locale.getDefault()).parse(date)
        } catch (e: Exception) {
            Date()
        }
    }
}

/**
 * All Date patterns should reside here, and hance reused
 */
object DatePatterns {
    const val SimpleDateFormat = "dd MMM yyyy"
}