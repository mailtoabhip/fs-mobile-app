package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

/**
 * Request model for downloading invoice document
 */
data class InvoiceDownloadRequest(
  @SerializedName("ticket_id")
  val ticketId: String
)
