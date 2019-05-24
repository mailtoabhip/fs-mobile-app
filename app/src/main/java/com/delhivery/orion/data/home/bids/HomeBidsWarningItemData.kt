package com.delhivery.orion.data.home.bids

import com.delhivery.orion.data.BaseKeyTypeModel

data class HomeBidsWarningItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeBidsWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val HomeBidsWarningItemDataKeyPrefix = "warning_"

/* actions */
const val HomeBidsWarningAction_SelectRoutes = "select_routes"
const val HomeBidsWarningAction_EditRoutePrefs = "edit_routes_prefs"