package com.dfd.delfin.api.repository

import com.dfd.delfin.api.request.InvoiceActionRequest
import com.dfd.delfin.api.request.InvoiceDownloadRequest
import com.dfd.delfin.api.response.InvoiceActionResponse
import com.dfd.delfin.api.response.InvoiceDetailsResponse
import com.dfd.delfin.api.service.InvoiceService
import com.dfd.delfin.utils.ErrorLogger
import com.dfd.delfin.utils.extensions.convertResponse
import io.reactivex.Single
import javax.inject.Inject

/**
 * Repository for invoice operations
 */
class InvoiceRepository @Inject constructor(
    private val invoiceService: InvoiceService, errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

  /**
   * Download invoice document by transaction ID
   */
  fun downloadInvoiceDocument(ticketId: String) =
    invoiceService.downloadInvoiceDocument(
      InvoiceDownloadRequest(ticketId = ticketId)
    ).convertResponse()

    /**
     * Get charge details for vendor review
     */
    fun getInvoiceDetails(ticketId: String): Single<InvoiceDetailsResponse> =
        invoiceService.getInvoiceDetails(ticketId).convertResponse()

    /**
     * Accept invoice with vendor invoice details
     */
    fun invoiceAction(
        actionRequest: InvoiceActionRequest
    ): Single<InvoiceActionResponse> =
        invoiceService.invoiceAction(actionRequest).convertResponse()


}
