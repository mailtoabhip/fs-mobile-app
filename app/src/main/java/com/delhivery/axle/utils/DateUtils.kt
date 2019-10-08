package com.delhivery.axle.utils

import androidx.annotation.IntRange
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.toCalendar
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

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
      val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
      dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
      dateFormatter.format(date)
    } catch (e: Exception) {
      e.printStackTrace()
      ""
    }
  }

  /**
   * @return Formatted ISO date to [format] in UTC
   */
  fun formatISODateToUTC(
    date: String,
    format: String
  ): String {
    return try {
      val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
      dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
      dateFormatter.format(ISO8601Utils.parse(date, ParsePosition(0)))
    } catch (e: Exception) {
      e.printStackTrace()
      ""
    }
  }

  /**
   * @return Format ISO date to [format]
   */
  fun formatISODate(
    date: String,
    format: String
  ): String {
    return try {
      val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
      dateFormatter.format(ISO8601Utils.parse(date, ParsePosition(0)))
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
      val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
      dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
      dateFormatter.parse(date)
    } catch (e: Exception) {
      Date()
    }
  }

  /**
   * @return parse date string and days diff of [date] from today
   */
  fun daysDiff(date: Date): Int {
    val cal = date.toCalendar()
    val now = Calendar.getInstance()
    return cal[Calendar.DAY_OF_YEAR] - now[Calendar.DAY_OF_YEAR]
  }

  /**
   * @return days diff of [date] string from today
   */
  fun daysDiffStr(
    date: String,
    format: String
  ) = daysDiffStr(parseDate(date, format))

  private fun daysDiffStr(
    requiredOn: Date
  ): String {
    val diff = daysDiff(requiredOn)
    return when {
      diff <= 0 -> "Today"
      diff == 1 -> "Tomorrow"
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
    return daysDiffStr(requiredOn)
  }

  /**
   * @return time stamp relative to current time
   */
  fun convertToRelativeTimeStamp(actionTime: String? = ""): String {
    return if (actionTime.isNotNullOrEmpty()) {
      (System.currentTimeMillis() - parseDate(
          actionTime!!, DatePatterns.OrionDateFormat
      ).time).let { msDiff ->
        val days = TimeUnit.MILLISECONDS.toDays(msDiff)
        val hours = TimeUnit.MILLISECONDS.toHours(msDiff - TimeUnit.DAYS.toMillis(days))
        val mins = TimeUnit.MILLISECONDS.toMinutes(msDiff - TimeUnit.HOURS.toMillis(hours))
        val secs = TimeUnit.MILLISECONDS.toSeconds(msDiff - TimeUnit.MINUTES.toMillis(mins))
        if (days > 0) {
          if (days <= 3) {
            "$days day ago"
          } else {
            formatDate(
                parseDate(actionTime, DatePatterns.OrionDateFormat), DatePatterns.SimpleDateFormat
            )
          }
        } else if (hours > 0) {
          "$hours hr $mins min ago"
        } else if (mins > 0) {
          "$mins min $secs s ago"
        } else {
          "Just now"
        }
      }
    } else {
      ""
    }
  }
}

/**
 * All Date patterns should reside here, and hance reused
 */
object DatePatterns {
  const val SimpleDateFormat = "dd MMM yyyy"
  const val OrionDateFormat = "yyyy-MM-dd'T'hh:mm:ss"
}