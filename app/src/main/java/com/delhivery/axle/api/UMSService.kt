package com.delhivery.axle.api

import com.delhivery.axle.api.request.OTPLoginRequest
import com.delhivery.axle.api.request.RequestOTP
import com.delhivery.axle.api.response.DelegationTokenResponse
import com.delhivery.axle.api.response.LoginResponse
import com.delhivery.axle.api.response.OTPSentResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Handle network calls to UMS Service
 */
interface UMSService {
  @POST("request-otp/")
  fun requestOTP(@Body request: RequestOTP): Single<OTPSentResponse>

  @POST("auth-otp-login/")
  fun otpLogin(@Body request: OTPLoginRequest): Single<LoginResponse>

  /**
   * Get delegation token for AWS
   */
  @GET("/v2/api/delegation-token/")
  fun getDelegationToken(
    @Query("target_id") targetId: String
  ): Single<DelegationTokenResponse>
}