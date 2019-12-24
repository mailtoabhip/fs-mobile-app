package com.delhivery.axle.ui.biddetails

import androidx.annotation.LayoutRes
import com.delhivery.axle.R
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.trips.TripDriverDetails

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
  val bids: List<TransactionBid>,
  val lowestAndUserBidPair: Pair<TransactionBid?, TransactionBid?>,
  val isPMTIndent: Boolean
) : BidDetailsUserBidState(R.layout.view_bid_details_place_bid)

/**
 * Edit bid, user bid found
 */
data class BidDetailsUserBidState_EditBid(
  val bidsCount: Int,
  val bids: List<TransactionBid>,
  val lowestAndUserBidPair: Pair<TransactionBid?, TransactionBid?>,
  val isPMTIndent: Boolean
) : BidDetailsUserBidState(R.layout.view_bid_details_edit_bid)

/**
 * Bids loading UI
 */
data class BidDetailsUserBidState_LoadingBids(
  val loading: Boolean = true
) : BidDetailsUserBidState(R.layout.view_bid_details_loading_bids)

/**
 * Confirmed bid state
 */
data class BidDetailsUserBidState_ConfirmedBid(
  var pickupLocation: String?,
  var driverDetails: TripDriverDetails?,
  var vehicleNumber: String?
) : BidDetailsUserBidState(R.layout.view_bid_details_confirmed_bid)

/**
 * Rejected bid state
 */
data class BidDetailsUserBidState_RejectedBid(
  val acceptedBid: TransactionBid,
  val userBid: TransactionBid,
  val isPMTIndent: Boolean
) : BidDetailsUserBidState(R.layout.view_bid_details_rejected_bid)