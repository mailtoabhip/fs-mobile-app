package com.delhivery.axle.data.gst

import com.delhivery.axle.data.BaseKeyTypeModel

data class GstProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = GstProgressItemDataKey
}

private const val GstProgressItemDataKey = "progress"