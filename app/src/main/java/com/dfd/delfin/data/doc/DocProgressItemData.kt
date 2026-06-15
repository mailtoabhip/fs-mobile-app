package com.dfd.delfin.data.doc

import com.dfd.delfin.data.BaseKeyTypeModel

data class DocProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = DocProgressItemDataKey
}

private const val DocProgressItemDataKey = "progress"