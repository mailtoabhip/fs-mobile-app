package com.delhivery.axle.api.response

import com.delhivery.axle.data.bids.TransactionBid
import com.google.gson.annotations.SerializedName

data class TransactionBidsResponseBody(
  @SerializedName("items") val bids: List<TransactionBid>,
  @SerializedName("total") val totalBids: Int
)

data class CreateTransactionBidResponse(
  @SerializedName("message") val message: String,
  @SerializedName("id") val bidId: String,
  @SerializedName("transaction_id") val transactionId: String
)

data class BidSummaryResponse(
  @SerializedName("confirmed_bids") val confirmedBids: Int,
  @SerializedName("my_bids") val myBids: Int,
  @SerializedName("lost_bids") val lostBids: Int
)

data class LowestBidResponse(
  @SerializedName("count") val numBids: Int,
  @SerializedName("min_price") val minBid: Double? = null,
  @SerializedName("transaction_id") val transactionId: String
)