package com.delhivery.axle.api.service

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.ChargesResponse
import com.delhivery.axle.api.response.CollectionResponse
import com.delhivery.axle.api.response.DNResponse
import com.delhivery.axle.api.response.*
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemData
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PayableService {
    /**
     * Get List Charges
     */
    @POST("list_charges")
    fun fetchChargeList(@Body payload: JsonObject): Single<BaseResponse<List<ChargesResponse>>>

    @POST("list_dns")
    fun fetchDNList(@Body payload: JsonObject): Single<BaseResponse<List<DNResponse>>>

    @POST("list_collections")
    fun  fetchCollectionList(@Body payload: JsonObject): Single<BaseResponse<List<CollectionResponse>>>

    @POST("list_consolidated_payments")
    fun fetchConsolidatedLedgerList(@Body payload: JsonObject): Single<BaseResponse<ConsolidatedLedgerResponse>>

    @POST("download_vendor_ledger")
    fun downloadVendorLedger(@Body payload: JsonObject): Single<BaseResponse<DownloadLedgerResponse>>

    @POST("email_vendor_ledger")
    fun emailVendorLedger(@Body payload: JsonObject): Single<BaseResponse<EmailLedgerResponse>>

    @GET("/list_invoices/{trip_id}")
    fun listInvoices(
            @Path("trip_id") tripId: String
    ):Single<BaseResponse<List<InvoiceListResponse>>>
}