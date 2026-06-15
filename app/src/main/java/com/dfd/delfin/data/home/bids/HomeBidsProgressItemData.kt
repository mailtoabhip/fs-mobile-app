package com.dfd.delfin.data.home.bids

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeBidsProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeBidsProgressItemDataKey
}

private const val HomeBidsProgressItemDataKey = "progress"