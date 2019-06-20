package com.delhivery.orion.data.home.loads

import com.delhivery.orion.data.BaseKeyTypeModel

data class HomeLoadsInfoItemData(
  val searchString: String = "",
  val editRouteString: String = ""
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeLoadsInfoItemDataKey
}

/* unique key for diff */
const val HomeLoadsInfoItemDataKey = "info"

/* actions */
const val HomeLoadsInfoAction_Search = "search"
const val HomeTripsHeaderAction_EditRoute = "edit_route"