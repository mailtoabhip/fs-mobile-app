package com.delhivery.orion.data.home.bids

import com.delhivery.orion.data.BaseKeyTypeModel

data class HomeBidsHeaderItemData(
  val myBids: Int = -1,
  val confirmedBid: Int = -1,
  val lostBids: Int = -1
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeBidsHeaderItemDataKey
}

/* unique key for diff */
const val HomeBidsHeaderItemDataKey = "header"

/* actions */
const val HomeBidsHeaderAction_MyBids = "my_bids"
const val HomeBidsHeaderAction_ConfirmedBids = "confirmed_bids"
const val HomeBidsHeaderAction_LostBids = "lost_bids"