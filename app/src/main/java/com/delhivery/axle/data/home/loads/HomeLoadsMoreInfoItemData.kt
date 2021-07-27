package com.delhivery.axle.data.home.loads

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeLoadsMoreInfoItemData(
  val editRouteString: String = ""
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeLoadsMoreInfoItemDataKey
}

/* unique key for diff */
const val HomeLoadsMoreInfoItemDataKey = "more_info"

/* actions */
const val HomeLoadsInfoAction_EditRoute = "edit_route"