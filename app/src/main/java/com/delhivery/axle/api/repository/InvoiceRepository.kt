package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.InvoiceActionRequest
import com.delhivery.axle.api.request.InvoiceDownloadRequest
import com.delhivery.axle.api.response.InvoiceActionResponse
import com.delhivery.axle.api.response.InvoiceDetailsResponse
import com.delhivery.axle.api.service.InvoiceService
import com.delhivery.axle.utils.ErrorLogger
import com.delhivery.axle.utils.extensions.convertResponse
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
