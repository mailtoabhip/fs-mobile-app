package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.*
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.GstNumberData
import com.delhivery.axle.api.response.PanVerificationResponse
import com.delhivery.axle.api.response.TransactionBidsResponseBody
import com.delhivery.axle.data.gst.GstDetailData
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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
}