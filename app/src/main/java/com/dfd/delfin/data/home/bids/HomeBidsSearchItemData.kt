package com.dfd.delfin.data.home.bids

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeBidsSearchItemData(
  val showing: Boolean = true,
  val query: String? = null
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeBidsSearchItemDataKey
}

/* unique key for diff */
private const val HomeBidsSearchItemDataKey = "search"

/* action id */
const val HomeBidsSearchAction_Search = "search"
const val HomeBidsSearchAction_Clear = "search_clear"

