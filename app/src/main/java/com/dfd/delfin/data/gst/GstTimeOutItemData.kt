package com.dfd.delfin.data.gst

import com.dfd.delfin.data.BaseKeyTypeModel

data class GstTimeOutItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = GstTimeOutItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val GstTimeOutItemDataKeyPrefix = "timeout_"

/* actions */
const val GstTimeOutAction = "time_out"