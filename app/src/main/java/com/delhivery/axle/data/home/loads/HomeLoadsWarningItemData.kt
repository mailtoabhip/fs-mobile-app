package com.delhivery.axle.data.home.loads

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeLoadsWarningItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeLoadsWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val HomeLoadsWarningItemDataKeyPrefix = "warning_"

/* actions */
const val HomeLoadsWarningAction_NoLoads = "no_loads"