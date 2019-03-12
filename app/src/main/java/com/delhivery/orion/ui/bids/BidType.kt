package com.delhivery.orion.ui.bids

enum class BidType(
  val typeId: Int,
  private val title: String
) {
  Unknown(-1, "na (%d"),
  ConfirmedBid(0, "Confirmed bids (%d)"),
  ActiveBid(1, "Active bids (%s)"),
  LostBid(2, "Lost bids (%d)");

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