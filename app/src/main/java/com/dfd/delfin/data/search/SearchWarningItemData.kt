package com.dfd.delfin.data.search

import com.dfd.delfin.data.BaseKeyTypeModel

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