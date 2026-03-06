package com.delhivery.axle.api.service

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.InvoiceDownloadData
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Invoice Service for downloading trip invoices
 */
interface InvoiceService {
  
  @POST("ticket_invoices/documents")
  fun downloadInvoiceDocument(@Body request: JsonObject): Single<BaseResponse<InvoiceDownloadData>>
}
