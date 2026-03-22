package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.InvoiceActionRequest
import com.delhivery.axle.api.request.InvoiceDownloadRequest
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.InvoiceActionResponse
import com.delhivery.axle.api.response.InvoiceDetailsResponse
import com.delhivery.axle.api.response.InvoiceDownloadData
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Invoice Service for downloading trip invoices
 */
interface InvoiceService {
  
  /**
   * Get charge details for vendor review
   */
  @GET("ticket_invoices/{ticket_id}")
  fun getInvoiceDetails(@Path("ticket_id") ticketId: String): Single<BaseResponse<InvoiceDetailsResponse>>
  
  /**
   * Accept or reject invoice
   */
  @POST("ticket_invoices/action")
  fun invoiceAction(@Body request: InvoiceActionRequest): Single<BaseResponse<InvoiceActionResponse>>
  
  /**
   * Download invoice document
   */
  @POST("ticket_invoices/documents")
  fun downloadInvoiceDocument(@Body invoiceDownloadRequest: InvoiceDownloadRequest): Single<BaseResponse<InvoiceDownloadData>>
}
