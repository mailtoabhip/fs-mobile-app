package com.delhivery.axle.data.doc

import com.delhivery.axle.data.BaseKeyTypeModel

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