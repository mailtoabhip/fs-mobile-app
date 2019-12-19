package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

/**
 * Payload request for [CreateTransactionBidRequest]
 */
data class CreateTransactionBidRequest(
  @SerializedName("transaction_id") val transactionId: String,
  @SerializedName("supplier_id") val supplierId: String,
  @SerializedName("supplier_name") val supplierName: String,
  @SerializedName("test_bid") val testUser: Boolean,
  @SerializedName("bidding_type") val commercialType: String? = "",
  @SerializedName("bid_price") val bidAmount: Int?,
  @SerializedName("freight_cost") val freightCost: Int?,
  @SerializedName("originator") val originator: String = "axle-app"
) {

  companion object {
    /**
     * @return [CreateTransactionBidRequest]
     */
    fun getRequest(
      isPMT: Boolean,
      transactionId: String,
      supplierId: String,
      supplierName: String,
      bidAmount: Int,
      pmtRate: Int,
      commercialType: String? = "",
      testUser: Boolean
    ) = if (isPMT)
      CreateTransactionBidRequest(
          transactionId = transactionId, supplierId = supplierId,
          supplierName = supplierName, freightCost = bidAmount, testUser = testUser,
          bidAmount = pmtRate, commercialType = commercialType
      )
    else
      CreateTransactionBidRequest(
          transactionId = transactionId, supplierId = supplierId, supplierName = supplierName,
          bidAmount = bidAmount, freightCost = bidAmount, testUser = testUser,
          commercialType = commercialType
      )
  }
}

/**
 * Payload request for [UpdateTransactionBidRequest]
 */
data class UpdateTransactionBidRequest(
  @SerializedName("transaction_id") val transactionId: String,
  @SerializedName("supplier_id") val supplierId: String,
  @SerializedName("bid_id") val bidId: String,
  @SerializedName("bidding_type") val commercialType: String? = "",
  @SerializedName("bid_price") val bidAmount: Int,
  @SerializedName("freight_cost") val freightCost: Int?,
  @SerializedName("action") val action: String = "bid_update"
) {

  companion object {
    /**
     * @return [CreateTransactionBidRequest]
     */
    fun getRequest(
      isPMT: Boolean,
      transactionId: String,
      bidId: String,
      amount: Int,
      supplierId: String,
      pmtRate: Int,
      commercialType: String
    ) = if (isPMT)
      UpdateTransactionBidRequest(
          transactionId = transactionId, bidId = bidId,
          bidAmount = pmtRate, freightCost = amount,
          supplierId = supplierId, commercialType = commercialType
      )
    else
      UpdateTransactionBidRequest(
          transactionId = transactionId, bidId = bidId,
          bidAmount = amount, freightCost = amount, supplierId = supplierId,
          commercialType = commercialType
      )
  }
}


