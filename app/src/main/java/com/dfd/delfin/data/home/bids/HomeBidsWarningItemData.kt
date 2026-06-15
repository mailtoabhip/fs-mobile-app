package com.dfd.delfin.data.home.bids

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeBidsWarningItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeBidsWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val HomeBidsWarningItemDataKeyPrefix = "warning_"

/* actions */
const val HomeBidsWarningAction_NoBids = "no_bids"