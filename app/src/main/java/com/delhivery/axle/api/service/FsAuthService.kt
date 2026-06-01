package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.FsInitiateRequest
import com.delhivery.axle.api.request.FsRefreshTokenRequest
import com.delhivery.axle.api.request.FsResendRequest
import com.delhivery.axle.api.request.FsUpdateProfileRequest
import com.delhivery.axle.api.request.FsVerifyRequest
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.FsInitiateData
import com.delhivery.axle.api.response.FsLogoutData
import com.delhivery.axle.api.response.FsRefreshTokenData
import com.delhivery.axle.api.response.FsResendData
import com.delhivery.axle.api.response.FsUpdateProfileData
import com.delhivery.axle.api.response.FsUserProfile
import com.delhivery.axle.api.response.FsVerifyData
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Retrofit service interface for the FS Auth Service.
 *
 * Two unified endpoints replace the old separate signup/login flows:
 *  - [initiate] handles both new and existing users
 *  - [verify]   handles both signup-verify and login-verify
 */
interface FsAuthService {

    /**
     * Initiates OTP flow for both new and existing users.
     * POST /api/v1/auth/initiate
     *
     * Response [FsInitiateData.isNewUser] distinguishes the two cases:
     *  - true  → new user, no session token in response
     *  - false → existing user, session token present
     */
    @POST("api/v1/auth/initiate")
    suspend fun initiate(
        @Body request: FsInitiateRequest
    ): BaseResponse<FsInitiateData>

    /**
     * Verifies OTP for both signup and login flows.
     * POST /api/v1/auth/verify
     *
     * [FsVerifyRequest.session] is required only for the login (existing user) case.
     */
    @POST("api/v1/auth/verify")
    suspend fun verify(
        @Body request: FsVerifyRequest
    ): BaseResponse<FsVerifyData>

    /**
     * Resends OTP for the given phone number.
     * POST /api/v1/auth/resend
     */
    @POST("api/v1/auth/resend")
    suspend fun resend(
        @Body request: FsResendRequest
    ): BaseResponse<FsResendData>

    /**
     * Revokes all refresh tokens for the user across all devices.
     * Requires Authorization: Bearer <access_token> header.
     * POST /api/v1/auth/logout
     */
    @POST("api/v1/auth/logout")
    suspend fun logout(): BaseResponse<FsLogoutData>

    /**
     * Returns the current user's profile from DFS DB.
     * Requires Authorization: Bearer <access_token> header.
     * GET /api/v1/auth/profile
     */
    @GET("api/v1/auth/profile")
    suspend fun getProfile(): BaseResponse<FsUserProfile>

    /**
     * Updates the user's first name, last name, and/or comm consent.
     * Requires Authorization: Bearer <access_token> header.
     * PUT /api/v1/auth/profile
     */
    @PUT("api/v1/auth/profile")
    suspend fun updateProfile(
        @Body request: FsUpdateProfileRequest
    ): BaseResponse<FsUpdateProfileData>

    /**
     * Generates a new access token and ID token using a valid refresh token.
     * POST /api/v1/auth/refresh
     */
    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: FsRefreshTokenRequest
    ): BaseResponse<FsRefreshTokenData>
}
