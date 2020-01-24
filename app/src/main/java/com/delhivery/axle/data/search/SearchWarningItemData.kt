package com.delhivery.axle.data.search

import com.delhivery.axle.data.BaseKeyTypeModel

data class SearchWarningItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = SearchWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val SearchWarningItemDataKeyPrefix = "warning_"

/* actions */
const val SearchWarningAction_NoResult = "no_results"