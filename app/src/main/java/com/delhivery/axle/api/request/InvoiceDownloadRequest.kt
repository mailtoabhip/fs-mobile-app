package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

/**
 * Request model for downloading invoice document
 */
data class InvoiceDownloadRequest(
  @SerializedName("orion_transaction_id")
  val orionTransactionId: String
)
