package com.delhivery.orion.api.response

import com.delhivery.orion.data.bids.TransactionBid
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