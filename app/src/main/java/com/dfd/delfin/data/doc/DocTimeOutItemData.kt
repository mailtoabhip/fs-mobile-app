package com.dfd.delfin.data.doc

import com.dfd.delfin.data.BaseKeyTypeModel

data class DocTimeOutItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = DocTimeOutItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val DocTimeOutItemDataKeyPrefix = "timeout_"

/* actions */
const val DocTimeOutAction = "time_out"