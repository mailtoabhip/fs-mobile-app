package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.OTPLoginRequest
import com.delhivery.axle.api.request.PasswordLoginRequest
import com.delhivery.axle.api.request.RequestOTP
import com.delhivery.axle.api.response.*
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
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

  /**
   * Get user roles and permissions from ums
   */
  @GET("/v2/applications/{app_id}/{ums_id}/permissions/")
  fun fetchUserRole(
    @Path("ums_id") umsId: String,
    @Path("app_id") appId: String
  ): Single<UMSRolePermissionResponse>

  /**
   * login using password
   */
  @POST("/v2/login/")
  fun requestPasswordVerification(
   @Body passwordRequest: PasswordLoginRequest
  ): Single<LoginPasswordResponse>


}