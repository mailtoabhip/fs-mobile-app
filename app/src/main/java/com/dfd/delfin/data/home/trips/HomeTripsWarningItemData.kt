package com.dfd.delfin.data.home.trips

import com.dfd.delfin.data.BaseKeyTypeModel

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
const val HomeTripsWarningAction_NoTrips = "no_trips"