package com.delhivery.orion.data.home.trips

import com.delhivery.orion.api.response.Summary
import com.delhivery.orion.data.BaseKeyTypeModel

data class HomeTripsHeaderItemData(
  val advancePending: Summary? = null,
  val balancePending: Summary? = null,
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
const val HomeTripsHeaderAction_BalancePending = "balance_pending"
const val HomeTripsHeaderAction_Completed = "completed"