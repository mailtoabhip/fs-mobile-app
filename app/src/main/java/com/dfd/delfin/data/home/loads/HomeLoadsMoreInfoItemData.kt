package com.dfd.delfin.data.home.loads

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeLoadsMoreInfoItemData(
  val editRouteString: String = ""
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeLoadsMoreInfoItemDataKey
}

/* unique key for diff */
const val HomeLoadsMoreInfoItemDataKey = "more_info"

/* actions */
const val HomeLoadsInfoAction_EditRoute = "edit_route"