package com.delhivery.axle.data.home.bids

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeBidsSearchItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeBidsSearchItemDataKey
}

/* unique key for diff */
private const val HomeBidsSearchItemDataKey = "search"

/* action id */
const val HomeBidsSearchAction_Search = "search"