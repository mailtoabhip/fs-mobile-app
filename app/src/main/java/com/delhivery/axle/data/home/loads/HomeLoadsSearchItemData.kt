package com.delhivery.axle.data.home.loads

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeLoadsSearchItemData(
  val query: String? = null
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeLoadsSearchItemDataKey
}

/* unique key for diff */
private const val HomeLoadsSearchItemDataKey = "search"

/* action id */
const val HomeLoadsSearchAction_Search = "search"