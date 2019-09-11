package com.delhivery.axle.data.home.trips

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeTripsSearchItemData(
  val query: String? = null
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeTripsSearchItemDataKey
}

/* unique key for diff */
private const val HomeTripsSearchItemDataKey = "search"

/* action id */
const val HomeTripsSearchAction_Search = "search"