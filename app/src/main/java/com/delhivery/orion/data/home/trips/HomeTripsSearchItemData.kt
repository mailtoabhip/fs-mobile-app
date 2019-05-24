package com.delhivery.orion.data.home.trips

import com.delhivery.orion.data.BaseKeyTypeModel

data class HomeTripsSearchItemData(
  val query: String? = null
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeTripsSearchItemDataKey
}

/* unique key for diff */
private const val HomeTripsSearchItemDataKey = "search"