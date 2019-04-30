package com.delhivery.orion.utils

import android.support.annotation.IntRange
import com.delhivery.orion.utils.extensions.toCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

  /**
   * Convert date to calendar, ie., [year], [month] and [dayOfMonth]
   *
   * @return Calendar with specified date
   */
  fun calendarFromDate(
    year: Int, @IntRange(from = 1, to = 12) month: Int, @IntRange(
        from = 1, to = 31
    ) dayOfMonth: Int
  ): Calendar {
    return Calendar.getInstance()
        .let {
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
  fun formatDate(
    date: Date,
    format: String
  ): String {
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
  fun parseDate(
    date: String,
    format: String
  ): Date {
    return try {
      SimpleDateFormat(format, Locale.getDefault()).parse(date)
    } catch (e: Exception) {
      Date()
    }
  }

  /**
   * Calculate days diff from today
   */
  fun daysDiff(date: Date): Int {
    val _cal = date.toCalendar()
    val _now = Calendar.getInstance()
    return _cal[Calendar.DAY_OF_YEAR] - _now[Calendar.DAY_OF_YEAR]
  }

  fun daysDiffStr(
    date: String,
    format: String
  ) = daysDiffStr(parseDate(date, format))

  /**
   * Days diff as string
   */
  fun daysDiffStr(
    requiredOn: Date
  ): String {
    val _diff = daysDiff(requiredOn)
    return when (_diff) {
      -1 -> "Yesterday"
      0 -> "Today"
      1 -> "Tomorrow"
      else -> formatDate(requiredOn, "dd MMM")
    }
  }

  /**
   * Days diff as string with time
   */
  fun daysDiffWithTimeStr(
    date: String,
    format: String
  ): String {
    val requiredOn = parseDate(date, format)
    val reqTime = formatDate(requiredOn, "hh:mm a")
    return "${daysDiffStr(requiredOn)}, $reqTime"
  }
}

/**
 * All Date patterns should reside here, and hance reused
 */
object DatePatterns {
  const val SimpleDateFormat = "dd MMM yyyy"
  const val OrionDateFormat = "yyyy-MM-dd'T'hh:mm:ss"
}