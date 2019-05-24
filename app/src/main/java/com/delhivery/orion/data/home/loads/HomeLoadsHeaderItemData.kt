package com.delhivery.orion.data.home.loads

import com.delhivery.orion.data.BaseKeyTypeModel

data class HomeLoadsHeaderItemData(
  val myBids: Int = -1,
  val confirmedBids: Int = -1
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeLoadsHeaderItemDataKey
}

/* unique key for diff */
const val HomeLoadsHeaderItemDataKey = "header"

/* actions */
//TODO: check the actions
//const val HomeBidsHeaderAction_MyBids = "my_bids"
//const val HomeBidsHeaderAction_ConfirmedBids = "confirmed_bids"