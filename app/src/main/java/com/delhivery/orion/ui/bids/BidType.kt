package com.delhivery.orion.ui.bids

import com.delhivery.orion.data.bids.TransactionBidStatus

enum class BidType(
  val typeId: Int,
  val status: TransactionBidStatus,
  private val title: String
) {
  Unknown(-1, TransactionBidStatus.NA, "na (%d"),
  ConfirmedBid(0, TransactionBidStatus.Accepted, "Confirmed bids (%d)"),
  ActiveBid(1, TransactionBidStatus.Open, "Active bids (%s)"),
  LostBid(2, TransactionBidStatus.Rejected, "Lost bids (%d)");

  /**
   * Get toolbar title with count of items
   */
  fun toolbarTitle(count: Int = 0) = String.format(title, count)

  companion object {
    /**
     * Get [BidType] by type id
     */
    fun byTypeId(typeId: Int) = values().filter { it.typeId == typeId }.firstOrNull() ?: Unknown
  }
}