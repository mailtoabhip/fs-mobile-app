package com.delhivery.axle.utils

import android.util.Log
import androidx.annotation.IntRange
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.toCalendar
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

  fun getUtcToIstFormatTime(utcTime: String?): String? {
    var ISTDateString = ""
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val pattern = "dd MMM yyyy hh:mm a"
    val formatter: SimpleDateFormat
    formatter = SimpleDateFormat(pattern)
    try {
      val ISTDate = sdf.parse(utcTime)
      formatter.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
      ISTDateString = formatter.format(ISTDate)
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    return ISTDateString
  }

  fun getUtcToIstFormatTimeOnly(utcTime: String?): String? {
    var ISTDateString = ""
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val pattern = "h:mm a"
    val formatter: SimpleDateFormat
    formatter = SimpleDateFormat(pattern)
    try {
      val ISTDate = sdf.parse(utcTime)
      formatter.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
      ISTDateString = formatter.format(ISTDate)
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    return ISTDateString
  }

  fun getIstFormatTimeOnly(istTime: Date): String? {
    var ISTDateString = ""
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val pattern = "h:mm a"
    val formatter: SimpleDateFormat
    formatter = SimpleDateFormat(pattern)
    try {
      formatter.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
      ISTDateString = formatter.format(istTime)
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    return ISTDateString
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

  fun timeDiff(startTime: Long, endTime: Long = Date().time): String {
    var milli: Long = endTime - startTime
    val secondsInMilli: Long = 1000
    val minutesInMilli = secondsInMilli * 60
    val hoursInMilli = minutesInMilli * 60
    val daysInMilli = hoursInMilli * 24

    val elapsedDays: Long = milli / daysInMilli
    milli %= daysInMilli

    val elapsedHrs = milli / hoursInMilli
    milli %= hoursInMilli

    val elapsedMinutes = milli / minutesInMilli
    milli %= minutesInMilli

    val elapsedSec = milli / secondsInMilli

    return when {
      elapsedDays.toInt() != 0 -> {
        "${elapsedDays.toInt()} ${if (elapsedDays.toInt() == 1) "day" else "days"}, " +
                "${elapsedHrs.toInt()} ${if (elapsedHrs.toInt() == 1) "hr" else "hrs"}"
      }
      elapsedHrs.toInt() != 0 -> {
        "${elapsedHrs.toInt()} ${if (elapsedHrs.toInt() == 1) "hr" else "hrs"}, " +
                "${elapsedMinutes.toInt()} ${if (elapsedMinutes.toInt() == 1) "min" else "mins"}"
      }
      elapsedMinutes.toInt() != 0 -> {
        "${elapsedMinutes.toInt()} ${if (elapsedMinutes.toInt() == 1) "min" else "mins"}, " +
                "${elapsedSec.toInt()} ${if (elapsedSec.toInt() == 1) "sec" else "secs"}"
      }
      else -> {
        "${elapsedSec.toInt()} ${if (elapsedSec.toInt() == 1) "sec" else "secs"}"
      }
    }
  }

  /**
   * @return parse date string and days diff of [date] from today
   */
  fun daysDiff(date: Date, now: Calendar = Calendar.getInstance()): Int {
    val cal = date.toCalendar()
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
      diff <= 0 && diff > -1 -> "Today"
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
    return daysDiffStr(parseDate(date, format))
  }

  fun daysDiffWithDateTimeStr(
          date: String,
          format: String
  ): String {
    return daysDiffWithTimeStr(parseDateIst(date, format))
  }

  fun parseDateIst(
          date: String,
          format: String
  ): Date {
    return try {
      val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
      dateFormatter.parse(date)!!
    } catch (e: Exception) {
      Log.i("Exception", e.toString())
      Date()
    }
  }

  fun daysDiffWithTimeStr(
          requiredOn: Date
  ): String {
    val diff = daysDiff(requiredOn)
    return when {
      diff <= 0 && diff > -1 -> "Today, ${getIstFormatTimeOnly(requiredOn)}"
      diff == 1 -> "Tomorrow, ${getIstFormatTimeOnly(requiredOn)}"
      else -> formatDate(requiredOn, "dd MMM 'T' hh:mm a")
    }
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
        when {
          days > 0 -> when {
            days <= 3 -> "$days day ago"
            else -> formatDate(
                    parseDate(actionTime, DatePatterns.OrionDateFormat), DatePatterns.SimpleDateFormat
            )
          }

          hours > 0 -> "$hours hr $mins min ago"
          mins > 0 -> "$mins min $secs s ago"
          else -> "Just now"
        }
      }
    } else {
      ""
    }
  }

  /**
   * @return time stamp relative to current time for trip
   */
  fun convertToRelativeTimeStampTrip(actionTime: String? = ""): String {
    return if (actionTime.isNotNullOrEmpty()) {
      (System.currentTimeMillis() - parseDate(
              actionTime!!, DatePatterns.OrionDateFormat
      ).time).let { msDiff ->
        val days = TimeUnit.MILLISECONDS.toDays(msDiff)
        val hours = TimeUnit.MILLISECONDS.toHours(msDiff - TimeUnit.DAYS.toMillis(days))
        val mins = TimeUnit.MILLISECONDS.toMinutes(msDiff - TimeUnit.HOURS.toMillis(hours))
        val secs = TimeUnit.MILLISECONDS.toSeconds(msDiff - TimeUnit.MINUTES.toMillis(mins))
        when {
          days > 0 -> when {
            days <= 1 -> "$days day"
            days in 2..3 -> "$days days"
            else -> "3 days+"
          }

          else -> "Less than 1 day"
        }
      }
    } else {
      ""
    }
  }

  fun getMonth(month: Int): String {
    when (month) {
      1 -> return "Jan"
      2 -> return "Feb"
      3 -> return "Mar"
      4 -> return "Apr"
      5 -> return "May"
      6 -> return "Jun"
      7 -> return "Jul"
      8 -> return "Aug"
      9 -> return "Sep"
      10 -> return "Oct"
      11 -> return "Nov"
      12 -> return "Dec"
    }
    return ""
  }

  fun presentDay(): String {
    val sdf = SimpleDateFormat("dd MMM");
    val resultdate = Date();
    return sdf.format(resultdate)
  }

  fun tomorrowDate(): String {
    var dt = Date()
    val c = Calendar.getInstance()
    c.time = dt
    c.add(Calendar.DATE, 1)
    dt = c.time
    val sdf = SimpleDateFormat("dd MMM");
    return sdf.format(dt)
  }

  fun presentTime(): String {
    var dt = Date()
    val c = Calendar.getInstance()
    c.time = dt
    dt = c.time
    val sdf = SimpleDateFormat("hh:mm a")
    return sdf.format(dt)
  }

  fun presentTimeInSlashFormat(): String {
    var dt = Date()
    val c = Calendar.getInstance()
    c.time = dt
    dt = c.time
    val sdf = SimpleDateFormat("dd/MM/yyyy")
    return sdf.format(dt)
  }


  fun getUtcToIstFormatTimeSlash(utcTime: String?): String? {
    var ISTDateString = ""
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val pattern = "dd/MM/yyyy"
    val formatter: SimpleDateFormat
    formatter = SimpleDateFormat(pattern)
    try {
      val ISTDate = sdf.parse(utcTime)
      formatter.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
      ISTDateString = formatter.format(ISTDate)
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    return ISTDateString
  }

  fun getUtcToIstFormatDateWithSuffix(utcTime: String?): String {
    var istDateString = ""
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val pattern = "dd MMM yyyy"
    val formatter: SimpleDateFormat
    formatter = SimpleDateFormat(pattern)
    try {
      val ISTDate = sdf.parse(utcTime)
      formatter.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
      istDateString = formatter.format(ISTDate)
      var dateString = istDateString.split(" ").get(0)
      var suffix = getDayOfMonthSuffix(dateString.toInt())
      dateString += suffix
      istDateString =
              dateString + " " + istDateString.split(" ").get(1) + " " + istDateString.split(" ").get(2)

    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    return istDateString
  }

  fun getISToUtcFormatDate(istTime: String?): String {
    var utctDateString = ""
    val sdf = SimpleDateFormat("dd/MM/yyyy")
    val pattern = "yyyy-MM-dd"
    val formatter: SimpleDateFormat
    formatter = SimpleDateFormat(pattern)
    try {
      val ISTDate = sdf.parse(istTime)
      utctDateString = formatter.format(ISTDate)
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    return utctDateString
  }

  fun getDayOfMonthSuffix(n: Int): String? {
    return if (n >= 11 && n <= 13) {
      "th"
    } else when (n % 10) {
      1 -> "st"
      2 -> "nd"
      3 -> "rd"
      else -> "th"
    }
  }

  fun getFormattedTimeIn12Hrs(time: String): String {
    val split = time.split(":")
    return if (split.size == 3) {
      if (split[0].toInt() < 12) {
        split[0] + ":" + split[1] + " AM"
      } else if (split[0].toInt() == 12) {
        split[0] + ":" + split[1] + " PM"
      } else if (split[0].toInt() == 24) {
        (split[0].toInt() - 12).toString() + ":" + split[1] + " AM"
      } else {
        if (split[0].toInt() - 12 < 10) "0" + (split[0].toInt() - 12).toString() + ":" + split[1] + " PM" else (split[0].toInt() - 12).toString() + ":" + split[1] + " PM"
      }
    } else {
      ""
    }
  }

  fun getTimeDiff(date: String): String {
    try {
      val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")

      val targetDate: Date? = dateFormat.parse(date)

      val targetCalendar = Calendar.getInstance()
      targetCalendar.time = targetDate

      val currentMillis = System.currentTimeMillis()

      val targetMillis = targetCalendar.timeInMillis

      val durationMillis = targetMillis - currentMillis

      val days = durationMillis / (1000 * 60 * 60 * 24)
      val hours = (durationMillis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
      val minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60)
      return when {
        days <= 0 -> when {
          hours <= 1 -> "0-2"
          hours in 2..3 -> "2-4"
          hours in 4..5 -> "4-6"
          hours in 5..11 -> "6-12"
          hours in 12..18 -> "12-18"
          else -> ""
        }

        else -> ""
      }
    } catch (e: Exception) {
      ""
    }
    return ""
  }

  fun getRelativeTimeDiff(statTime:String, endTime:String){

  }

  fun formatFastagTransactionDate(dateStr: String): String {
    return try {
      val inputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
      val date = inputFormat.parse(dateStr)
      
      if (date != null) {
        // Get day with ordinal suffix (20th, 21st, etc.)
        val calendar = Calendar.getInstance()
        calendar.time = date
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val suffix = getDayOfMonthSuffix(day)
        
        // Format: "20th Jan 2026, 4:36PM"
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mma", Locale.getDefault())
        
        "$day$suffix ${monthFormat.format(date)} ${yearFormat.format(date)}, ${timeFormat.format(date)}"
      } else {
        dateStr
      }
    } catch (e: Exception) {
      Log.e("DateUtils", "Error parsing FASTag date: $dateStr", e)
      dateStr
    }
  }
  
  fun formatFastagTransactionDateShort(dateStr: String): String {
    return try {
      // Input format: "20-01-2026 16:36:38"
      val inputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
      val date = inputFormat.parse(dateStr)
      
      if (date != null) {
        // Get day with ordinal suffix (20th, 21st, etc.)
        val calendar = Calendar.getInstance()
        calendar.time = date
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val suffix = getDayOfMonthSuffix(day)
        
        // Format: "20th Jan 4:36PM"
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mma", Locale.getDefault())
        
        "$day$suffix ${monthFormat.format(date)} ${timeFormat.format(date)}"
      } else {
        dateStr
      }
    } catch (e: Exception) {
      Log.e("DateUtils", "Error parsing FASTag date: $dateStr", e)
      dateStr
    }
  }
}

/**
 * All Date patterns should reside here, and hance reused
 */
object DatePatterns {
  const val SimpleDateFormat = "dd MMM yyyy"
  const val CurrentStatusFormat = "dd MMM yy"
  const val PODDateFormat = "yyyy-MM-dd"
  const val OrionDateFormat = "yyyy-MM-dd'T'hh:mm:ss"
}