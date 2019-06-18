package com.delhivery.orion.data.home.trips

import com.delhivery.orion.data.BaseKeyTypeModel

data class HomeTripsHeaderItemData(
  val advancePending: Int = -1,
  val balancePending: Int = -1,
  val inTransit: Int = -1,
  val completed: Int = -1
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