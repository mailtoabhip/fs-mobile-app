package com.delhivery.axle.data.home.bids

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeBidsProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeBidsProgressItemDataKey
}

private const val HomeBidsProgressItemDataKey = "progress"