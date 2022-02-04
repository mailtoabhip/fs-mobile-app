package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.*
import com.delhivery.axle.api.response.*
import com.delhivery.axle.data.UserModel
import com.delhivery.axle.data.UserRespone
import com.delhivery.axle.data.gst.GstDetailData
import io.reactivex.Single
import retrofit2.http.*

interface LoadBoardService {
    /**
     * verify Pan Card Number
     */
    @GET("/validate_pan_card")
    fun validatePanNumber(@Body panVerificationRequest: PanVerificationRequest)
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
    ): Single<BaseResponse<GstDetailData>>

    /**
     * get GST OTP
     */
    @POST("/generate_otp")
    fun getGstOtp(
            @Body request: GstOtpGetRequest
    ): Single<BaseResponse<Any>>

    /**
     * verify GST OTP
     */
    @POST("/validate_otp")
    fun verifyGstOtp(
            @Body request: GstOtpVerifyRequest
    ): Single<BaseResponse<Any>>

    /**
     * verify GST via doc upload
     */
    @POST("/validate_ocr")
    fun verifyByDocUpload(
            @Body request: GstDocRequest
    ): Single<BaseResponse<Any>>

    @POST("/get_otp")
    fun requestOTP(@Body request: RequestOTP): Single<OTPSentResponse>

    @POST("/verify_otp")
    fun otpLogin(@Body request: OTPLoginRequest): Single<BaseResponse<LoginResponse>>

    @PATCH("/update_user")
    fun updateUser(@Body request: UpdateUserRequest): Single<BaseMessageResponse>

    /**
     * Get user details
     */
    @GET("/get_user")
    fun userDetails(
            @Query("uuid") userId: String
    ): Single<BaseResponse<UserRespone>>
}