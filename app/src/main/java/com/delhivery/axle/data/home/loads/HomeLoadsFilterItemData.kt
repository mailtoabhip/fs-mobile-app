package com.delhivery.axle.data.home.loads

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeLoadsFilterItemData(
  var actionLabel: Boolean,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeLoadsFilterItemDataKeyPrefix + actionId
}

/* unique key for filter */
const val HomeLoadsFilterItemDataKeyPrefix = "filter_"

/* actions */
const val HomeLoadsFilterAction = "filter"