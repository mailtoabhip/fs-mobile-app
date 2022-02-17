package com.delhivery.axle.data.doc

import com.delhivery.axle.data.BaseKeyTypeModel

data class DocProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = DocProgressItemDataKey
}

private const val DocProgressItemDataKey = "progress"