package com.delhivery.axle.data.search

import com.delhivery.axle.data.BaseKeyTypeModel

data class SearchTimeOutItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = SearchTimeOutItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val SearchTimeOutItemDataKeyPrefix = "timeout_"

/* actions */
const val SearchTimeOutAction = "time_out"