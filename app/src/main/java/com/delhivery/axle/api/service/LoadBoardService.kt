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
    ): Single<BaseResponse<Any>>

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
        @Body request: AddAddressRequest
    ): Single<BaseResponse<Any>>

    /*
     * update your communication address
     */

    @POST("/submit_address")
    fun updateNewAddress(
        @Body request: UpdateAddressVerificationRequest
    ): Single<BaseResponse<Any>>

    /**
     * patch user details
     */
    @GET("/get_user")
    fun userDetails(
            @Query("uuid") userId: String
    ): Single<BaseResponse<UserRespone>>

}