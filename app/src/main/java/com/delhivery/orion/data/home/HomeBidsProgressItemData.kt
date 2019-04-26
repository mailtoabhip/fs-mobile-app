package com.delhivery.orion.data.home

import com.delhivery.orion.data.BaseKeyTypeModel

data class HomeBidsProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeBidsProgressItemDataKey
}

private const val HomeBidsProgressItemDataKey = "progress"