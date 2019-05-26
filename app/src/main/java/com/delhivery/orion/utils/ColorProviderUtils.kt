package com.delhivery.orion.utils

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import com.delhivery.orion.R

object ColorProviderUtils {
  /**
   * Get truck type drawable
   */
  @DrawableRes
  fun truckTypeDrawableRes(containerType: String?) = when (containerType) {
    "closed" -> R.drawable.ic_closed_truck
    else -> R.drawable.ic_open_truck
  }

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
    "confirmed"-> R.color.status_confirmed
    else -> R.color.status_lost
  }
}