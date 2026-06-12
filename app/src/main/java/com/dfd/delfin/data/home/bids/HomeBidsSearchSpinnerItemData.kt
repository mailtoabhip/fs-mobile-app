package com.dfd.delfin.data.home.bids

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeBidsSearchSpinnerItemData(
  val isIntracity: Boolean = false
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeBidsSearchSpinnerItemDataKey
}

/* unique key for diff */
private const val HomeBidsSearchSpinnerItemDataKey = "search_spinner"