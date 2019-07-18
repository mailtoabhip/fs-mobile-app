package com.delhivery.axle.data.home.routes

import com.delhivery.axle.data.BaseKeyTypeModel

data class RoutesProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = RoutesProgressItemDataKey
}

private const val RoutesProgressItemDataKey = "progress"