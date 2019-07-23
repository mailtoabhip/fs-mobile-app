package com.delhivery.axle.data.home.trips

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeTripsProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeTripsProgressItemDataKey
}

private const val HomeTripsProgressItemDataKey = "progress"