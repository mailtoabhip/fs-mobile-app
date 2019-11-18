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
  @SerializedName("commercial_type") val commercialType: String? = "",
  @SerializedName("vendor_pmt_rate") val pmtRate: Int? = 0,
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
      testUser: Boolean,
      commercialType: String? = ""
    ) = if (isPMT)
      CreateTransactionBidRequest(
          transactionId = transactionId, supplierId = supplierId,
          supplierName = supplierName, bidAmount = bidAmount, testUser = testUser
      )
    else
      CreateTransactionBidRequest(
          transactionId, supplierId, supplierName, bidAmount, testUser,
          commercialType
      )
  }
}

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


