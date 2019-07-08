package com.delhivery.orion.data.home.trips

import com.delhivery.orion.data.BaseKeyTypeModel

data class HomeTripsTimeOutItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeTripsTimeOutItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val HomeTripsTimeOutItemDataKeyPrefix = "timeout_"

/* actions */
const val HomeTripsTimeOutAction = "time_out"