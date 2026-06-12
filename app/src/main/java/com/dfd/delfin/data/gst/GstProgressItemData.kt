package com.dfd.delfin.data.gst

import com.dfd.delfin.data.BaseKeyTypeModel

data class GstProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = GstProgressItemDataKey
}

private const val GstProgressItemDataKey = "progress"