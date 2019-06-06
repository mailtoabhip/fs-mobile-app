package com.delhivery.orion.data.home.loads

import com.delhivery.orion.data.BaseKeyTypeModel

data class HomeLoadsProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeLoadsProgressItemDataKey
}

private const val HomeLoadsProgressItemDataKey = "progress"