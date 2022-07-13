package com.delhivery.axle.data.sharerates

import com.delhivery.axle.data.BaseKeyTypeModel

/**
 * Timeout item data for Share Rates list
 */
data class ShareRatesTimeOutItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = ShareRatesItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val ShareRatesItemDataKeyPrefix = "timeout_"

/* actions */
const val ShareRatesTimeOutAction = "time_out"