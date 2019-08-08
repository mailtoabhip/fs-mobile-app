package com.delhivery.axle.utils

import androidx.annotation.DrawableRes
import com.delhivery.axle.R

object DrawableProviderUtils {
  /**
   * Get truck type drawable
   */
  @DrawableRes
  fun truckTypeDrawableRes(containerType: String?) = when (containerType) {
    "closed" -> R.drawable.ic_closed_truck
    "open" -> R.drawable.ic_open_truck
    else -> R.drawable.ic_trailer_truck
  }

  /**
   * Get days diff background
   */
  @DrawableRes
  fun daysDiffBgDrawableRes(
    date: String,
    format: String
  ): Int {
    val diff = DateUtils.daysDiff(DateUtils.parseDate(date, format))
    if (diff <= 0) {
      return R.drawable.bg_date_today
    } else {
      return R.drawable.bg_date_tomorrow
    }
  }
}