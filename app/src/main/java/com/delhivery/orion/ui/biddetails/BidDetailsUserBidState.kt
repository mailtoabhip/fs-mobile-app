package com.delhivery.orion.ui.biddetails

import android.support.annotation.LayoutRes
import com.delhivery.orion.R
import com.delhivery.orion.data.bids.TransactionBid

/**
 * Base state with [containerId]
 */
abstract class BidDetailsUserBidState(@LayoutRes val containerId: Int)

/**
 * Place bid first, no bids found
 */
data class BidDetailsUserBidState_PlaceBidFirst(val bidsCount: Int = 0) : BidDetailsUserBidState(
    R.layout.view_bid_details_place_bid_first
)

/**
 * Place bid, other bids found
 */
data class BidDetailsUserBidState_PlaceBid(
  val bidsCount: Int,
  val bids: List<TransactionBid>
) : BidDetailsUserBidState(R.layout.view_bid_details_place_bid)

/**
 * Edit bid, user bid found
 */
data class BidDetailsUserBidState_EditBid(
  val bidsCount: Int,
  val bids: List<TransactionBid>,
  val userBid: TransactionBid
) : BidDetailsUserBidState(R.layout.view_bid_details_edit_bid)

/**
 * Bids loading UI
 */
data class BidDetailsUserBidState_LoadingBids(
  val loading: Boolean = true
) : BidDetailsUserBidState(R.layout.view_bid_details_loading_bids)