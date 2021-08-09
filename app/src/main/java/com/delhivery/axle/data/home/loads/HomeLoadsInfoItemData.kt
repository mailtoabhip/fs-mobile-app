package com.delhivery.axle.data.home.loads

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeLoadsInfoItemData(
  val searchString: String = ""
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeLoadsInfoItemDataKey
}

/* unique key for diff */
const val HomeLoadsInfoItemDataKey = "info"

/* actions */
const val HomeLoadsInfoAction_Search = "search_info"