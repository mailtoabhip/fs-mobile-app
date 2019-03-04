package com.delhivery.orion.api

import com.delhivery.orion.api.request.OTPLoginRequest
import com.delhivery.orion.api.request.RequestOTP
import com.delhivery.orion.api.response.LoginResponse
import com.delhivery.orion.api.response.OTPSentResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * UMS Service
 */
interface UMSService {
  @POST("request-otp/")
  fun requestOTP(@Body request: RequestOTP): Single<OTPSentResponse>

  @POST("auth-otp-login/")
  fun otpLogin(@Body request: OTPLoginRequest): Single<LoginResponse>
}