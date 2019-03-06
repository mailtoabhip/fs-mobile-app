package com.delhivery.orion.data.home

import com.delhivery.orion.data.BaseKeyTypeModel

data class HomeBidsSearchItemData(
  val loadRequests: Int = -1
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeBidsSearchItemDataKey
}

/* unique key for diff */
private const val HomeBidsSearchItemDataKey = "search"