package com.delhivery.axle.ui.bids

import com.delhivery.axle.data.bids.TransactionBidStatus

/**
 *  Enum to hold various Bid types
 */
enum class BidType(
  val typeId: Int,
  val status: TransactionBidStatus,
  private val title: String
) {
  Unknown(-1, TransactionBidStatus.NA, "na"),
  ConfirmedBid(0, TransactionBidStatus.Accepted, "Confirmed bids"),
  ActiveBid(1, TransactionBidStatus.Open, "Active bids"),
  LostBid(2, TransactionBidStatus.Rejected, "Lost bids");

  /**
   * Get toolbar title with count of items
   */
  fun toolbarTitle(count: Int = 0) = when (count) {
    0 -> title
    else -> "$title($count)"
  }

  companion object {
    /**
     * Get [BidType] by type id
     */
    fun byTypeId(typeId: Int) = values().filter { it.typeId == typeId }.firstOrNull() ?: Unknown
  }
}