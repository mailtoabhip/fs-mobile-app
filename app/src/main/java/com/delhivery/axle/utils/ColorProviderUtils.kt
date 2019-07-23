package com.delhivery.axle.utils

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.delhivery.axle.R

object ColorProviderUtils {

  /**
   * Get days diff background
   */
  @DrawableRes
  fun daysDiffBgDrawableRes(
    date: String,
    format: String
  ) = when (DateUtils.daysDiff(DateUtils.parseDate(date, format))) {
    0 -> R.drawable.bg_date_today
    1 -> R.drawable.bg_date_tomorrow
    else -> R.drawable.bg_date_others
  }

  @ColorRes
  fun getStatusColor(
    status: String
  ) = when (status.toLowerCase()) {
    "active" -> R.color.status_active
    "confirmed" -> R.color.status_confirmed
    else -> R.color.status_lost
  }

  @ColorRes
  fun getTripStatusColor(
    status: String
  ) = when (status.toLowerCase()) {
    "advance pending", "balance pending" -> R.color.pending
    "intransit" -> R.color.status_active
    else -> R.color.status_confirmed
  }
}