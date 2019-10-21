package com.delhivery.axle.api

import com.delhivery.axle.api.request.OTPLoginRequest
import com.delhivery.axle.api.request.RequestOTP
import com.delhivery.axle.api.response.LoginResponse
import com.delhivery.axle.api.response.OTPSentResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Handle network calls to UMS Service
 */
interface UMSService {
  @POST("request-otp/")
  fun requestOTP(@Body request: RequestOTP): Single<OTPSentResponse>

  @POST("auth-otp-login/")
  fun otpLogin(@Body request: OTPLoginRequest): Single<LoginResponse>
}