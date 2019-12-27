package com.delhivery.axle.utils

import androidx.annotation.DrawableRes
import com.delhivery.axle.R
import com.delhivery.axle.data.transactions.TransactionChannel
import com.delhivery.axle.data.transactions.TransactionChannel.HPCL
import com.delhivery.axle.data.transactions.TransactionChannel.IOCL
import com.delhivery.axle.data.transactions.TransactionType
import com.delhivery.axle.data.transactions.TransactionType.ADVANCE_CREDIT
import com.delhivery.axle.data.transactions.TransactionType.DEBIT
import com.delhivery.axle.data.transactions.TransactionType.PETRO_CASHBACK_CREDIT
import com.delhivery.axle.data.transactions.TransactionType.PETRO_CASHBACK_DEBIT
import com.delhivery.axle.data.transactions.TransactionType.RECONCILIATION_DEBIT

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

  @DrawableRes
  fun expandedRes(flag: Boolean) = if (flag) {
    R.drawable.ic_collapse
  } else {
    R.drawable.ic_expand
  }
}