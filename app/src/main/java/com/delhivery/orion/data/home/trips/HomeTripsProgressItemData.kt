package com.delhivery.orion.data.home.trips

import com.delhivery.orion.data.BaseKeyTypeModel

data class HomeTripsProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeTripsProgressItemDataKey
}

private const val HomeTripsProgressItemDataKey = "progress"