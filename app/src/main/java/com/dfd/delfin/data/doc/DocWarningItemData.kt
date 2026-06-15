package com.dfd.delfin.data.doc

import com.dfd.delfin.data.BaseKeyTypeModel

data class DocWarningItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = DocWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val DocWarningItemDataKeyPrefix = "warning_"

/* actions */
const val DocWarningAction_NoResult = "no_results"