package com.delhivery.axle.data.gst

import com.delhivery.axle.data.BaseKeyTypeModel

data class GstWarningItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = GstWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val GstWarningItemDataKeyPrefix = "warning_"

/* actions */
const val GstWarningAction_NoResult = "no_results"