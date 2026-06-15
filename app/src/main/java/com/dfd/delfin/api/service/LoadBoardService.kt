package com.dfd.delfin.api.service

import com.dfd.delfin.api.request.*
import com.dfd.delfin.api.response.*
import com.dfd.delfin.data.gst.GstDetailItemData
import com.google.gson.JsonObject
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface LoadBoardService {
    /**
     * verify Pan Card Number
     */
    @POST("/validate_pan_card")
    fun validatePanNumber(@Body request: PanVerificationRequest)
            : Single<BaseResponse<PanVerificationResponse>>

    /**
     * verify GST Numbers
     */
    @POST("/gsts_by_pan")
    fun getGstNumbers(
            @Body request: GstNumberRequest
    ): Single<BaseResponse<GstNumberData>>

    /**
     * get GST Details
     */
    @POST("/gst_by_number")
    fun getGstDetails(
            @Body request: GstDetailRequest
    ): Single<BaseResponse<GstDetailItemData>>

    /**
     * get GST OTP
     */
    @POST("/generate_otp")
    fun getGstOrAadhaarOtp(
            @Body request: GstOrAadhaarOtpGetRequest
    ): Single<BaseMessageResponse>

    /**
     * verify GST OTP
     */
    @POST("/validate_otp")
    fun verifyGstOrAadhaarOtp(
            @Body request: GstOrAadhaarOtpVerifyRequest
    ): Single<BaseMessageResponse>

    /**
     * verify GST via doc upload
     */
    @POST("/validate_ocr")
    fun verifyByDocUpload(
            @Body request: GstOrAadhaarDocRequest
    ): Single<BaseResponse<DocUploadResponse>>

    @POST("/get_otp")
    fun requestOTP(@Body request: RequestOTP): Single<OTPSentResponse>

    @POST("/verify_otp")
    fun otpLogin(@Body request: OTPLoginRequest): Single<BaseResponse<LoginResponse>>

    @PATCH("/update_user")
    fun updateUser(@Body request: UpdateUserRequest): Single<BaseMessageResponse>

    @POST("/reset_kyc")
    fun resetKyc(@Body request: ResetKycDataRequest): Single<BaseMessageResponse>

  /**
     * add alternate address
     */
    @POST("/add_address")
    fun addAddress(
        @Body request: AddAddressModel
    ): Single<BaseMessageResponse>

    /*
     * update your communication address
     */

    @POST("/submit_address")
    fun updateNewAddress(
        @Body request: UpdateAddressVerificationRequest
    ): Single<BaseMessageResponse>

    /**
     * patch user details
     */
    @GET("api/v1/auth/profile")
    fun userDetails(): Single<BaseResponse<FsUserProfile>>

    /**
     * kyc docs
     */
    @GET("/user_documents/{uuid}")
    fun kycDocs(
            @Path("uuid") userId: String
    ): Single<BaseResponse<KycDocsResponse>>

   /**
     * verify business with RC
     */
    @POST("/validate_rc")
    fun validateRC(
        @Body request: RcVerificationRequest
    ): Single<BaseResponse<RcVerificationResponse>>

    /**
     * upload document for business verification
     */
    @POST("/upload_document")
    fun uploadDocument(
        @Body request: VerificationDocUploadRequest
    ): Single<BaseMessageResponse>

    /**
     * Get team member's detail
     */
    @GET("/get_child_users/{uuid}")
    fun getTeamMembers(
            @Path("uuid") userId: String
    ): Single<BaseResponse<TeamDetailResponse>>

    /**
     * Create secondary user
     */
    @POST("/create_child_user")
    fun createSecondaryUser(
            @Body payload: JsonObject
    ): Single<BaseMessageResponse>

    /**
     * Update secondary user
     */
    @PATCH("/update_user")
    fun updateSecondaryUser(
            @Body payload: JsonObject
    ): Single<BaseMessageResponse>

    /**
     * Update Admin user
     */
    @PATCH("/update_user")
    fun updateAdminUser(
            @Body payload: JsonObject
    ): Single<BaseMessageResponse>


    /**
     * Get KYC detail
     */
    @GET("/get_kyc_details/{uuid}")
    fun getKYCDetails(
            @Path("uuid") userId: String
    ): Single<BaseResponse<KYCDetailResponse>>

  /**
   * Add Route details
   */
  @POST("/routes/{uuid}")
  fun addRoute(
    @Path("uuid") userId: String,
    @Body updateRouteRequest: UpdateRouteRequest
  ): Single<BaseMessageResponse>

  /**
   * Edit Route details
   */
  @PATCH("/routes/{uuid}")
  fun editRoute(
    @Path("uuid") userId: String,
    @Body updateRouteRequest: UpdateRouteRequest
  ): Single<BaseMessageResponse>

  /**
   * Delete Route Details
   */
  @HTTP(method = "DELETE", path = "/routes/{uuid}", hasBody = true)
  fun deleteRoute(
    @Path("uuid") userId: String,
    @Body updateRouteRequest: UpdateRouteRequest
  ): Single<BaseMessageResponse>

  /**
   * get popular locations
   */
  @GET("/popular_locations")
  fun getPopularLocations(
    @Query("user_id") userId: String
  ): Single<BaseResponse<List<PopularLocationsResponse>>>

  @POST("/validate_bank")
  fun getBankName(
    @Body bankValidationRequest: BankValidationRequest
  ): Single<BaseResponse<BankValidationResponse>>

    /**
     * Get delegation token for AWS
     */
    @GET("/delegation-token")
    fun getDelegationToken(
        @Query("target_id") targetId: String
    ): Single<DelegationTokenResponse>


    /**
     * Get FASTag balance
     */
    @GET("finance/fastag/balance-check")
    fun getFastagBalance(
        @Query("fastag_id") tagId: String
    ): Single<BaseResponse<FastagBalanceResponse>>

    @GET("/finance/fastag/transactions/download")
    @Streaming
    fun downloadFastagTransactions(
        @Query("fastag_id") tagId: String,
        @Query("from_date") fromDate: String?,
        @Query("to_date") toDate: String?
    ): Single<ResponseBody>

    /**
     * Submit FASTag lead request
     */
    @POST("/finance/fastag/lead")
    fun submitFastagLead(
        @Body request: FastagLeadRequest
    ): Single<BaseResponse<FastagLeadResponse>>


    /**
     * Get FASTag status
     */
    @GET("/finance/fastag/status")
    fun fetchFastagStatus(
        @Query("tag_id") tagId: String
    ): Single<BaseResponse<FastagStatusResponse>>

    @GET("/finance/fastag/transaction-dispute")
    fun getTransactionDispute(
        @Query("txn_id") txnId: String?
    ): Single<BaseResponse<TransactionDisputeResponse>>

    /**
     * Get FASTag transactions by toll plaza
     * New API endpoint for transaction selection
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
     * Submit dispute with multipart form data
     */
    @Multipart
    @POST("finance/fastag/transactions/dispute")
    fun submitDispute(
        @Part txnId: MultipartBody.Part?=null,
        @Part tollPlazaId: MultipartBody.Part?=null,
        @Part refundAmount: MultipartBody.Part?=null,
        @Part comment: MultipartBody.Part?=null,
        @Part raisedAgainst: MultipartBody.Part?=null,
        @Part additionalTxnId: MultipartBody.Part?=null,
        @Part uploadDoc1: MultipartBody.Part?=null,
        @Part uploadDoc2: MultipartBody.Part?=null,
        @Part uploadDoc3: MultipartBody.Part?=null
    ): Single<BaseResponse<DisputeSubmissionResponse>>

    @GET("finance/fastag/transactions/dispute/categories")
    fun getDisputeIssuesList(
        @Query("partner") partner: String
    ): Single<BaseResponse<DisputeIssuesResponse>>

    @GET("finance/fastag/transaction/dispute/form-config")
    fun getDisputeFormConfig(
        @Query("disputeTypeCode") disputeTypeCode: String
    ): Single<BaseResponse<List<FormField>>>

    /**
     * Get service groups for onboarding.
     * Uses suspend function for Flow-based architecture.
     *
     * @param vendorId Vendor UUID for eligibility-based group visibility
     * @return BaseResponse<ServiceGroupsResponse> with list of service groups
     */
    @GET("/api/v1/onboarding/service-groups")
    suspend fun getServiceGroups(
        @Header("X-Vendor-Id") vendorId: String
    ): BaseResponse<ServiceGroupsResponse>

    /**
     * Get service requirements/documents for a specific service.
     * Uses suspend function for Flow-based architecture.
     *
     * @param serviceId The service identifier (e.g., "svc_fastag")
     * @return BaseResponse<ServiceRequirementsResponse> with sections and documents
     */
    @GET("/api/v1/onboarding/services/{service_id}/requirements")
    suspend fun getServiceRequirements(
        @Path("service_id") serviceId: String
    ): BaseResponse<ServiceRequirementsResponse>
}