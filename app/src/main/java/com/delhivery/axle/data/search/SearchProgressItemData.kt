package com.delhivery.axle.data.search

import com.delhivery.axle.data.BaseKeyTypeModel

data class SearchProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = SearchProgressItemDataKey
}

private const val SearchProgressItemDataKey = "progress"