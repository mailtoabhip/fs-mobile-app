package com.delhivery.axle.data.home.loads

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeLoadsProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeLoadsProgressItemDataKey
}

private const val HomeLoadsProgressItemDataKey = "progress"