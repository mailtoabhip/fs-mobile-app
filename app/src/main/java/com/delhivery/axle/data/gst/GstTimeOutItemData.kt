package com.delhivery.axle.data.gst

import com.delhivery.axle.data.BaseKeyTypeModel

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