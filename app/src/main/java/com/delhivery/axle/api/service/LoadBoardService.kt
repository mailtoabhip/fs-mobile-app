package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.*
import com.delhivery.axle.api.response.BaseMessageResponse
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.GstNumberData
import com.delhivery.axle.api.response.PanVerificationResponse
import com.delhivery.axle.api.response.*
import com.delhivery.axle.data.UserModel
import com.delhivery.axle.data.UserRespone
import com.delhivery.axle.data.gst.GstDetailData
import com.delhivery.axle.data.gst.GstDetailItemData
import com.google.gson.JsonObject
import io.reactivex.Single
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
    @GET("/get_user")
    fun userDetails(
            @Query("uuid") userId: String
    ): Single<BaseResponse<UserRespone>>

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
    ): Single<BaseResponse<UserDetailResponse>>

    /**
     * Create secondary user
     */
    @POST("/create_child_user")
    fun createSecondaryUser(
            @Body payload: JsonObject
    ): Single<BaseResponse<CreateUserResponse>>

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


}