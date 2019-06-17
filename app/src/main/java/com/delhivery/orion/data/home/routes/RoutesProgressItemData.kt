package com.delhivery.orion.data.home.routes

import com.delhivery.orion.data.BaseKeyTypeModel

data class RoutesProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = RoutesProgressItemDataKey
}

private const val RoutesProgressItemDataKey = "progress"