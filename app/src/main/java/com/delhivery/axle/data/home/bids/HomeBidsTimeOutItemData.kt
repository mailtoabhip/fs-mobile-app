package com.delhivery.axle.data.home.bids

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeBidsTimeOutItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeBidsTimeOutItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val HomeBidsTimeOutItemDataKeyPrefix = "timeout_"

/* actions */
const val HomeBidsTimeOutAction = "time_out"