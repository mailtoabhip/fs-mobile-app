package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

/**
 * Payload request for [CreateTransactionBidRequest]
 */
data class CreateTransactionBidRequest(
  @SerializedName("transaction_id") val transactionId: String,
  @SerializedName("supplier_id") val supplierId: String,
  @SerializedName("supplier_name") val supplierName: String,
  @SerializedName("bid_price") val bidAmount: Int,
  @SerializedName("test_bid") val testUser: Boolean,
  @SerializedName("originator") val originator: String = "axle-app"
)

/**
 * Payload request for [UpdateTransactionBidRequest]
 */
data class UpdateTransactionBidRequest(
  @SerializedName("transaction_id") val transactionId: String,
  @SerializedName("bid_id") val bidId: String,
  @SerializedName("bid_price") val bidAmount: Int,
  @SerializedName("supplier_id") val supplierId: String,
  @SerializedName("action") val action: String = "bid_update"
)

