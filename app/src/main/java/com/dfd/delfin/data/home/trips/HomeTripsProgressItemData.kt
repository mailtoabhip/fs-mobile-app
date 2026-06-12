package com.dfd.delfin.data.home.trips

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeTripsProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeTripsProgressItemDataKey
}

private const val HomeTripsProgressItemDataKey = "progress"