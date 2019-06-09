package com.delhivery.orion.data.home.loads

import com.delhivery.orion.data.BaseKeyTypeModel

data class HomeLoadsSearchItemData(
  val query: String? = null
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeLoadsSearchItemDataKey
}

/* unique key for diff */
private const val HomeLoadsSearchItemDataKey = "search"

/* action id */
const val HomeLoadsSearchAction_Search = "search"