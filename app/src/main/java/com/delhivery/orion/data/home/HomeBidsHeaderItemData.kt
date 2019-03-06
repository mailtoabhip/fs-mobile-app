package com.delhivery.orion.data.home

import com.delhivery.orion.data.BaseKeyTypeModel

data class HomeBidsHeaderItemData(
  val myBids: Int = -1,
  val confirmedBids: Int = -1
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeBidsHeaderItemDataKey
}

/* unique key for diff */
private const val HomeBidsHeaderItemDataKey = "header"