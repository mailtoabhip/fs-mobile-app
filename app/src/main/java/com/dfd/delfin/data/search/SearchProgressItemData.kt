package com.dfd.delfin.data.search

import com.dfd.delfin.data.BaseKeyTypeModel

data class SearchProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = SearchProgressItemDataKey
}

private const val SearchProgressItemDataKey = "progress"