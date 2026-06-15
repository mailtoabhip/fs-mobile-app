package com.dfd.delfin.data.home.pod

import com.dfd.delfin.data.BaseKeyTypeModel

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
const val HomePodWarningAction_TimeOut = "time_out"