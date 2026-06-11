package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

/**
 * Request body for POST /api/v1/auth/initiate
 * Handles both new-user (signup) and existing-user (login) flows.
 */
data class FsInitiateRequest(
    @SerializedName("phone") val phone: String
)

/**
 * Request body for POST /api/v1/auth/verify
 * Handles both signup-verify and login-verify flows.
 * [session] is only required for the login case (existing user).
 */
data class FsVerifyRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("session") val session: String? = null
)

/**
 * Request body for PUT /api/v1/auth/profile
 * At least one field must be provided.
 */
data class FsUpdateProfileRequest(
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("middle_name") val middleName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("comm_consent") val commConsent: Boolean? = null
)

/**
 * Request body for POST /api/v1/auth/resend
 */
data class FsResendRequest(
    @SerializedName("phone") val phone: String
)

/**
 * Request body for POST /api/v1/auth/refresh
 */
data class FsRefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)
