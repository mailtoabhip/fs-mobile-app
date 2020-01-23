package com.delhivery.axle.data.home.pod

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomePodWarningItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = HomePodWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val HomePodWarningItemDataKeyPrefix = "warning_"

/* actions */
const val HomePodWarningAction_NoTrips = "no_pod_trips"