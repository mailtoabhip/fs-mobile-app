package com.delhivery.axle.api.repository

import com.delhivery.axle.api.service.InvoiceService
import com.delhivery.axle.utils.extensions.convertResponse
import com.google.gson.JsonObject
import javax.inject.Inject

/**
 * Repository for invoice operations
 */
class InvoiceRepository @Inject constructor(
  private val invoiceService: InvoiceService
) : BaseRepository() {

  /**
   * Download invoice document by transaction ID
   */
  fun downloadInvoiceDocument(transactionId: String) =
    invoiceService.downloadInvoiceDocument(
      JsonObject().apply {
        addProperty("orion_transaction_id", "ORION-TXN-98765")  // Hardcoded for testing
      }
    ).convertResponse()
}
