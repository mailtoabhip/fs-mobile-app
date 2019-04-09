package com.delhivery.orion.api.request

import com.google.gson.annotations.SerializedName

data class CreateTransactionBidRequest(
  @SerializedName("transaction_id") val transactionId: String,
  @SerializedName("supplier_id") val supplierId: String,
  @SerializedName("supplier_name") val supplierName: String,
  @SerializedName("bid_price") val bidAmount: Int
)

data class UpdateTransactionBidRequest(
  @SerializedName("transaction_id") val transactionId: String,
  @SerializedName("bid_price") val bidAmount: Int
)

