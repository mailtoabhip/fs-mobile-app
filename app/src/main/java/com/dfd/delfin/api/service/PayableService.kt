package com.dfd.delfin.api.service

import com.dfd.delfin.api.response.BaseResponse
import com.dfd.delfin.api.response.ChargesResponse
import com.dfd.delfin.api.response.CollectionResponse
import com.dfd.delfin.api.response.DNResponse
import com.dfd.delfin.api.response.*
import com.google.gson.JsonObject
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

    @GET("list_invoices/{trip_id}")
    fun listInvoices(
            @Path("trip_id") tripId: String
    ):Single<BaseResponse<List<InvoiceListResponse>>>

    @POST("list_dn_recovery")
    fun listDNRecoveries(@Body payload: JsonObject): Single<BaseResponse<List<DNRecoveryResponse>>>

    @POST("list_overpayment_recovery")
    fun listOverpaymentRecoveries(@Body payload: JsonObject): Single<BaseResponse<List<OverpaymentRecoveryResponse>>>

    @POST("list_trip_recoveries")
    fun listTripRecoveries(@Body payload: JsonObject): Single<BaseResponse<List<TripRecoveryResponse>>>
}