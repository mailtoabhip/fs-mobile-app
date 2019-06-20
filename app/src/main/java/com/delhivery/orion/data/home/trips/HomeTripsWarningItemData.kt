package com.delhivery.orion.data.home.trips

import com.delhivery.orion.data.BaseKeyTypeModel

data class HomeTripsWarningItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeTripsWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val HomeTripsWarningItemDataKeyPrefix = "warning_"

/* actions */
const val HomeTripsWarningAction_NoLoads = "no_loads"