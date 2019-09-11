package com.delhivery.axle.data.home.loads

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeLoadsTimeOutItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeLoadsTimeOutItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val HomeLoadsTimeOutItemDataKeyPrefix = "timeout_"

/* actions */
const val HomeLoadsTimeOutAction = "time_out"