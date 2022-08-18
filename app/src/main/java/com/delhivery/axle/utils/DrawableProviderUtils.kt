package com.delhivery.axle.utils

import androidx.annotation.DrawableRes
import com.delhivery.axle.R
import com.delhivery.axle.data.home.trips.PODStatus
import com.delhivery.axle.data.home.trips.PODStatus.REJECT
import com.delhivery.axle.data.home.trips.PODStatus.REVIEW
import com.delhivery.axle.data.transactions.TransactionChannel
import com.delhivery.axle.data.transactions.TransactionChannel.HPCL
import com.delhivery.axle.data.transactions.TransactionChannel.IOCL
import com.delhivery.axle.data.transactions.TransactionType
import com.delhivery.axle.data.transactions.TransactionType.ADVANCE_CREDIT
import com.delhivery.axle.data.transactions.TransactionType.DEBIT
import com.delhivery.axle.data.transactions.TransactionType.PETRO_CASHBACK_CREDIT
import com.delhivery.axle.data.transactions.TransactionType.PETRO_CASHBACK_DEBIT
import com.delhivery.axle.data.transactions.TransactionType.RECONCILIATION_DEBIT

/**
 * Helper class to set drawables/backgrounds
 */
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
   * Get transaction type drawable
   */
  @DrawableRes
  fun transactionTypeDrawableRes(
    type: TransactionType,
    channel: TransactionChannel
  ) = when (type) {
    DEBIT -> {
      when (channel) {
        IOCL -> R.drawable.ic_fuel
        HPCL -> R.drawable.ic_fuel
        else -> R.drawable.ic_bank
      }
    }
    ADVANCE_CREDIT -> R.drawable.ic_rupee_indian
    PETRO_CASHBACK_CREDIT, PETRO_CASHBACK_DEBIT -> R.drawable.ic_fuel
    RECONCILIATION_DEBIT -> R.drawable.ic_wallet
    else -> R.drawable.ic_wallet
  }

  /**
   * Get days diff background
   */
  @DrawableRes
  fun daysDiffBgDrawableRes(
    date: String,
    format: String
  ): Int {
    return if (DateUtils.daysDiff(DateUtils.parseDate(date, format)) <= 0) {
      R.drawable.bg_date_today
    } else {
      R.drawable.bg_date_tomorrow
    }
  }
  /**
   * Get days diff drawable
   */
  @DrawableRes
  fun daysDiffBgDrawableResDraw(
    date: String,
    format: String
  ): Int {
    return if (DateUtils.daysDiff(DateUtils.parseDate(date, format)) <= 0) {
      var hours=DateUtils.parseDate(date,format).hours
      if(hours>=1 && hours<12){
        R.drawable.ic_vector_morning
      }else if(hours>=12 && hours<16){
        R.drawable.ic_vector_afternoon
      }else if(hours>=16 && hours<=21){
        R.drawable.ic_vector_eve
      }else {
        R.drawable.ic_vector_eve
      }    } else {
      var hours=DateUtils.parseDate(date,format).hours
      if(hours>=1 && hours<12){
        R.drawable.ic_vector_morning
      }else if(hours>=12 && hours<16){
        R.drawable.ic_vector_afternoon
      }else if(hours>=16 && hours<=21){
        R.drawable.ic_vector_eve
      }else {
        R.drawable.background_rectangle_border_curved
      }
    }
  }

  /**
   * Get pod drawable
   */
  fun podDrawableRes(type: PODStatus): Int {
    return when (type) {
      REJECT -> {
        R.drawable.bg_pod_reject
      }
      REVIEW -> {
        R.drawable.bg_pod_under_review
      }
      else -> {
        R.drawable.bg_load_action_btn
      }
    }
  }

  /**
   * Expandable resource for consolidated recycler view
   */

  @DrawableRes
  fun expandedRes(flag: Boolean) = if (flag) {
    R.drawable.ic_collapse
  } else {
    R.drawable.ic_expand
  }

  /**
   * Expandable resource for consolidated recycler view
   */

  @DrawableRes
  fun expandedResLedger(flag: Boolean) = if (flag) {
    R.drawable.ic_baseline_remove_24
  } else {
    R.drawable.ic_add_black_24dp

  }

  /**
   * Resource for showing current trip status
   */
  @DrawableRes
  fun tripStatusRes(flag: Boolean) = if (flag) {
    R.drawable.ic_check_circle_green
  } else {
    R.drawable.ic_rounded_circle_black
  }

  /**
   * Resource for showing rewards details
   */
  @DrawableRes
  fun rewardsFullDetailsRes(flag: Boolean) = if (flag) {
    R.drawable.ic_vector_up_arrow
  } else {
    R.drawable.ic_vector_down_arrow
  }


}