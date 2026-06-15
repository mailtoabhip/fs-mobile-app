package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

/**
 * Data model for invoice download URL
 */
data class InvoiceDownloadData(
  @SerializedName("url")
  val url: String? = null
)
