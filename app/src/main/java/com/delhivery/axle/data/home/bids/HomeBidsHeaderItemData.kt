package com.delhivery.axle.data.home.bids

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.ui.bids.BidType

data class HomeBidsHeaderItemData(
  val myBids: Int = 0,
  val confirmedBid: Int = 0,
  val lostBids: Int = 0,
  val contractBids: Int = 0,
  var bidType: BidType = BidType.ActiveBid
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeBidsHeaderItemDataKey
}

/* unique key for diff */
const val HomeBidsHeaderItemDataKey = "header"

/* actions */
const val HomeBidsHeaderAction_MyBids = "my_bids"
const val HomeBidsHeaderAction_ConfirmedBids = "confirmed_bids"
const val HomeBidsHeaderAction_LostBids = "lost_bids"
const val HomeBidsHeaderAction_ContractBids = "contract_bids"

/* new tab change actions */
const val HomeBidsHeaderAction_TabChangeActive = "tab_change_active"
const val HomeBidsHeaderAction_TabChangeConfirmed = "tab_change_confirmed"
const val HomeBidsHeaderAction_TabChangeLost = "tab_change_lost"


/**
 * New data class and other attributes to incorporate the new header as per Axle App Revamp
 * 15-07-2025
 */
//data class HomeBidsNewHeaderItemData(
//  val myBids: Int = -1,
//  val confirmedBid: Int = -1,
//  val lostBids: Int = -1,
//  val contractBids: Int = -1
//) : BaseKeyTypeModel<String>() {
//  override fun key() = HomeBidsNewHeaderItemDataKey
//}
//
///* unique key for diff */
//const val HomeBidsNewHeaderItemDataKey = "header"
//
///* actions */
//const val HomeBidsHeaderAction_MyBids = "my_bids"
//const val HomeBidsHeaderAction_ConfirmedBids = "confirmed_bids"
//const val HomeBidsHeaderAction_LostBids = "lost_bids"
//const val HomeBidsHeaderAction_ContractBids = "contract_bids"


