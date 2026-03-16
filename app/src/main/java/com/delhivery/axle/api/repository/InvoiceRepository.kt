package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.InvoiceDownloadRequest
import com.delhivery.axle.api.service.InvoiceService
import com.delhivery.axle.utils.extensions.convertResponse
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
      InvoiceDownloadRequest(orionTransactionId = transactionId)
    ).convertResponse()
}
