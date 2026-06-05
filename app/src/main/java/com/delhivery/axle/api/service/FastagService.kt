package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.FastagLeadRequest
import com.delhivery.axle.api.request.FastagRechargeRequest
import com.delhivery.axle.api.request.IssueTagRequest
import com.delhivery.axle.api.response.*
import com.google.gson.JsonObject
import com.delhivery.axle.ui.fastag.tagAssignment.pendingActions.PendingActionsResponse
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * Retrofit service interface for all FASTag-related API endpoints.
 * Handles truck listing, balance checks, transactions, recharge, disputes, and leads.
 */
interface FastagService {

    /**
     * Get FASTag vehicle listing (v2).
     * Returns paginated list of vehicles with FASTag linked.
     */
    @GET("fastag/v1/listing")
    fun getFastagListing(
        @Query("limit") limit: Int?,
        @Query("offset") offset: Int?
    ): Single<BaseResponse<FastagListingResponse>>

    /**
     * Get FASTag balance for a specific tag.
     */
    @GET("/fastag/v1/balance-check")
    fun getFastagBalance(
        @Query("fastag_id") tagId: String
    ): Single<BaseResponse<FastagBalanceResponse>>

    /**
     * Get FASTag transactions listing.
     */
    @GET("/fastag/v1/transactions/listing")
    fun getFastagTransactions(
        @Query("fastag_id") tagId: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Single<BaseResponse<FastagTransactionResponse>>

    /**
     * Download FASTag transactions as file.
     */
    @GET("/fastag/v1/transactions/download")
    @Streaming
    fun downloadFastagTransactions(
        @Query("fastag_id") tagId: String,
        @Query("from_date") fromDate: String?,
        @Query("to_date") toDate: String?
    ): Single<ResponseBody>

    /**
     * Submit FASTag lead request (buy FASTag).
     */
    @POST("/fastag/v1/lead")
    fun submitFastagLead(
        @Body request: FastagLeadRequest
    ): Single<BaseResponse<FastagLeadResponse>>

    /**
     * Recharge FASTag from wallet.
     */
    @POST("/fastag/v1/recharge")
    fun rechargeFastag(
        @Body request: FastagRechargeRequest
    ): Single<BaseResponse<FastagRechargeResponse>>

    /**
     * Get FASTag status for a specific tag.
     */
    @GET("/fastag/v1/status")
    fun fetchFastagStatus(
        @Query("tag_id") tagId: String
    ): Single<BaseResponse<FastagStatusResponse>>

    /**
     * Get FASTag transactions by toll plaza.
     */
    @GET("/fastag/v1/transactions/search/toll-plaza")
    fun getFastagTransactionsByTollPlaza(
        @Query("toll_plaza_id") tollPlazaId: String,
        @Query("dateTime") dateTime: String?,
        @Query("limit") limit: Int?,
        @Query("offset") offset: Int?,
        @Query("fastag_id") fastagId: String?,
        @Query("txn_id") txnId: String?
    ): Single<BaseResponse<FastagTransactionsByTollPlazaResponse>>

    /**
     * Get transaction dispute details.
     */
    @GET("/fastag/v1/transaction-dispute")
    fun getTransactionDispute(
        @Query("txn_id") txnId: String?
    ): Single<BaseResponse<TransactionDisputeResponse>>

    /**
     * Get dispute issues list.
     */
    @GET("/fastag/v1/transactions/dispute/categories")
    fun getDisputeIssuesList(
        @Query("partner") partner: String
    ): Single<BaseResponse<DisputeIssuesResponse>>

    /**
     * Get dispute form configuration.
     */
    @GET("/fastag/v1/transaction/dispute/form-config")
    fun getDisputeFormConfig(
        @Query("disputeTypeCode") disputeTypeCode: String
    ): Single<BaseResponse<List<FormField>>>

    /**
     * Lookup barcode from dispatch table.
     */
    @GET("v1/barcode")
    suspend fun barcodeLookup(
        @Query("order_id") orderId: String,
        @Query("order_item_id") orderItemId: Int,
        @Query("vehicle_class") vehicleClass: String
    ): BaseResponse<BarcodeLookupResponse>

    /**
     * Search products and barcodes from IDFC.
     */
    @POST("finance/fastag/issuance/product-barcode")
    suspend fun searchProductBarcode(
        @Body request: com.delhivery.axle.api.request.ProductBarcodeRequest
    ): BaseResponse<ProductBarcodeResponse>

    /**
     * Generate consent OTP for tag mapping.
     */
    @POST("finance/fastag/issuance/generate-otp")
    suspend fun generateOtp(
        @Body request: com.delhivery.axle.api.request.GenerateOtpRequest
    ): BaseResponse<Any>

    /**
     * Issue FASTag and process payment.
     */
    @POST("finance/fastag/issuance/issue-tag")
    suspend fun issueTag(
        @Body request: IssueTagRequest
    ): BaseResponse<IssueTagResponse>

    /**
     * Submit dispute with multipart form data.
     */
    @Multipart
    @POST("/fastag/v1/transactions/dispute")
    fun submitDispute(
        @Part txnId: MultipartBody.Part? = null,
        @Part tollPlazaId: MultipartBody.Part? = null,
        @Part refundAmount: MultipartBody.Part? = null,
        @Part comment: MultipartBody.Part? = null,
        @Part raisedAgainst: MultipartBody.Part? = null,
        @Part additionalTxnId: MultipartBody.Part? = null,
        @Part uploadDoc1: MultipartBody.Part? = null,
        @Part uploadDoc2: MultipartBody.Part? = null,
        @Part uploadDoc3: MultipartBody.Part? = null
    ): Single<BaseResponse<DisputeSubmissionResponse>>

    /**
     * Fetch recharge status.
     */
    @POST("/api/v1/wallet/recharge-status")
    fun fetchRechargeStatus(
        @Header("x-user-id") userId: String,
        @Body request: JsonObject
    ): Single<BaseResponse<RechargeStatusResponse>>



    /**
     * Get pending actions for FASTag tag issuance.
     * GET /fastag/tag-issuance/v1/pending-actions
     * TODO : update the endpoints with correctly mapped BE url
     */
    @GET("/fastag/tag-issuance/v1/pending-actions")
    suspend fun getPendingActions(
        @Header("X-Vendor-Id") vendorId: String
    ): BaseResponse<PendingActionsResponse>
}
