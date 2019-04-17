package com.delhivery.orion.data

import com.delhivery.orion.data.home.TripStatus
import com.delhivery.orion.utils.DatePatterns
import com.delhivery.orion.utils.DateUtils
import com.google.gson.annotations.SerializedName
import java.util.concurrent.TimeUnit

data class TripHistoryModel(
  @SerializedName("action_time") val actionTime: String,
  @SerializedName("trip_status") private val _tripStatus: String,
  @SerializedName("user_name") val userName: String
) {
  /**
   * Trip Status [TripStatus]
   */
  fun status() = TripStatus.byKey(_tripStatus)

  /**
   * Compute epoch to action time
   */
  fun epoch() = (System.currentTimeMillis() - DateUtils.parseDate(
      actionTime, DatePatterns.OrionDateFormat
  ).time).let { msDiff ->
    val days = TimeUnit.MILLISECONDS.toDays(msDiff)
    val hours = TimeUnit.MILLISECONDS.toHours(msDiff - TimeUnit.DAYS.toMillis(days))
    val mins = TimeUnit.MILLISECONDS.toMinutes(msDiff - TimeUnit.HOURS.toMillis(hours))
    val secs = TimeUnit.MILLISECONDS.toSeconds(msDiff - TimeUnit.MINUTES.toMillis(mins))
    if (days > 0) {
      "$days day ago"
    } else if (hours > 0) {
      "$hours hr $mins min ago"
    } else if (mins > 0) {
      "$mins min $secs s ago"
    } else {
      "Just now"
    }
  }
}