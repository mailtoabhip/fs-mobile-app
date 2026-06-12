package com.dfd.delfin.data.home.trips

import com.dfd.delfin.api.response.Summary
import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeTripsHeaderItemData(
  val advancePending: Summary? = null,
  val podPending: Summary? = null,
  val inTransit: Summary? = null,
  val completed: Summary? = null
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeTripsHeaderItemDataKey
}

/* unique key for diff */
const val HomeTripsHeaderItemDataKey = "header"

/* actions */
const val HomeTripsHeaderAction_AdvancePending = "advance_pending"
const val HomeTripsHeaderAction_InTransit = "in_transit"
const val HomeTripsHeaderAction_PODPending = "pod_pending"
const val HomeTripsHeaderAction_Completed = "completed"