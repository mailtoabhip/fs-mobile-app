package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.FastagLeadRequest
import com.delhivery.axle.api.request.FastagRechargeRequest
import com.delhivery.axle.api.response.*
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
    @GET("v2/fastag/listing")
    fun getFastagListing(
        @Query("limit") limit: Int?,
        @Query("offset") offset: Int?
    ): Single<BaseResponse<FastagListingResponse>>

    /**
     * Get FASTag balance for a specific tag.
     */
    @GET("finance/fastag/balance-check")
    fun getFastagBalance(
        @Query("fastag_id") tagId: String
    ): Single<BaseResponse<FastagBalanceResponse>>

    /**
     * Get FASTag transactions listing.
     */
    @GET("/finance/fastag/transactions/listing")
    fun getFastagTransactions(
        @Query("fastag_id") tagId: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Single<BaseResponse<FastagTransactionResponse>>

    /**
     * Download FASTag transactions as file.
     */
    @GET("/finance/fastag/transactions/download")
    @Streaming
    fun downloadFastagTransactions(
        @Query("fastag_id") tagId: String,
        @Query("from_date") fromDate: String?,
        @Query("to_date") toDate: String?
    ): Single<ResponseBody>

    /**
     * Submit FASTag lead request (buy FASTag).
     */
    @POST("/finance/fastag/lead")
    fun submitFastagLead(
        @Body request: FastagLeadRequest
    ): Single<BaseResponse<FastagLeadResponse>>

    /**
     * Recharge FASTag from wallet.
     */
    @POST("/finance/users/wallet/fastag/recharge")
    fun rechargeFastag(
        @Body request: FastagRechargeRequest
    ): Single<BaseResponse<FastagRechargeResponse>>

    /**
     * Get FASTag status for a specific tag.
     */
    @GET("/finance/fastag/status")
    fun fetchFastagStatus(
        @Query("tag_id") tagId: String
    ): Single<BaseResponse<FastagStatusResponse>>

    /**
     * Get FASTag transactions by toll plaza.
     */
    @GET("finance/fastag/transactions/search/toll-plaza")
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
    @GET("/finance/fastag/transaction-dispute")
    fun getTransactionDispute(
        @Query("txn_id") txnId: String?
    ): Single<BaseResponse<TransactionDisputeResponse>>

    /**
     * Get dispute issues list.
     */
    @GET("finance/fastag/transactions/dispute/categories")
    fun getDisputeIssuesList(
        @Query("partner") partner: String
    ): Single<BaseResponse<DisputeIssuesResponse>>

    /**
     * Get dispute form configuration.
     */
    @GET("finance/fastag/transaction/dispute/form-config")
    fun getDisputeFormConfig(
        @Query("disputeTypeCode") disputeTypeCode: String
    ): Single<BaseResponse<List<FormField>>>

    /**
     * Lookup barcode from dispatch table.
     */
    @GET("finance/fastag/barcodeLookup")
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
        @Body request: com.delhivery.axle.api.request.IssueTagRequest
    ): BaseResponse<IssueTagResponse>

    /**
     * Submit dispute with multipart form data.
     */
    @Multipart
    @POST("finance/fastag/transactions/dispute")
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
}
