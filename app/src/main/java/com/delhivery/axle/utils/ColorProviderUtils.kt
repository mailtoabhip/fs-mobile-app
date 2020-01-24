package com.delhivery.axle.utils

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.delhivery.axle.R
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

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

  /**
   * Get bid status text color
   */
  @ColorRes
  fun getStatusColor(
    status: String
  ) = when (status.toLowerCase()) {
    "active" -> R.color.status_active
    "confirmed" -> R.color.status_confirmed
    else -> R.color.status_lost
  }

  /**
   * Get trip status text color
   */
  @ColorRes
  fun getTripStatusColor(
    status: String
  ) = when (status.toLowerCase()) {
    "advance pending", "balance pending" -> R.color.pending
    "intransit" -> R.color.status_active
    else -> R.color.status_confirmed
  }

  /**
   * Get promise date text color
   */
  @ColorRes
  fun getPromiseDateColor(
    timeDiff: Long
  ) = if (timeDiff > 0) R.color.status_lost
  else R.color.sub_heading_black

  /**
   * Get bank transaction amount text color
   */
  @ColorRes
  fun getTransactionAmountColor(
    status: String
  ) = if (status.toLowerCase().contains("debit")) {
    R.color.status_lost
  } else {
    R.color.status_confirmed
  }

  /**
   * Get bank transaction status text color
   */
  @ColorRes
  fun getTransactionStatusColor(
    status: String
  ) = when (status.toLowerCase()) {
    "processing", "pending" -> R.color.status_active
    "failed", "rejected" -> R.color.status_lost
    else -> R.color.status_confirmed
  }

  /**
   * Get pod date diff text color
   */
  @ColorRes
  fun getPODDateColor(
    time: Date
  ): Int {
    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    val diffInMillisec = today.timeInMillis - time.time
    val daysDiff = TimeUnit.MILLISECONDS.toDays(diffInMillisec)
        .toInt()
    return when (daysDiff) {
      in 0..9 -> R.color.status_confirmed
      else -> R.color.status_lost
    }
  }
}