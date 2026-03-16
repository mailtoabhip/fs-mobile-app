package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.InvoiceDownloadRequest
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.InvoiceDownloadData
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Invoice Service for downloading trip invoices
 */
interface InvoiceService {
  
  @POST("ticket_invoices/documents")
  fun downloadInvoiceDocument(@Body invoiceDownloadRequest: InvoiceDownloadRequest): Single<BaseResponse<InvoiceDownloadData>>
}
