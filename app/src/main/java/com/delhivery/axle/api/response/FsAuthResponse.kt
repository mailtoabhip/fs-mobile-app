package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

// ---------------------------------------------------------------------------
// Shared sub-models
// ---------------------------------------------------------------------------

/**
 * User profile returned by GET /api/v1/auth/profile and PUT /api/v1/auth/profile.
 */
data class FsUserProfile(
    @SerializedName("id") val id: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    @SerializedName("is_active") val isActive: Boolean?,
    @SerializedName("comm_consent") val commConsent: Boolean?,
    @SerializedName("created_at") val createdAt: String?
)

// ---------------------------------------------------------------------------
// Initiate  —  POST /api/v1/auth/initiate
// ---------------------------------------------------------------------------

/**
 * `data` payload for the initiate response.
 *
 * New user (signup) case: [session] is null, [isNewUser] is true.
 * Existing user (login) case: [session] is populated, [isNewUser] is false.
 */
data class FsInitiateData(
    @SerializedName("message") val message: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("session") val session: String?,
    @SerializedName("is_new_user") val isNewUser: Boolean?
)

// ---------------------------------------------------------------------------
// Verify  —  POST /api/v1/auth/verify
// ---------------------------------------------------------------------------

/**
 * `data` payload for the verify response.
 * Returned for both signup-verify and login-verify.
 */
data class FsVerifyData(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("id_token") val idToken: String,
    @SerializedName("is_new_user") val isNewUser: Boolean?
)

// ---------------------------------------------------------------------------
// Logout  —  POST /api/v1/auth/logout
// ---------------------------------------------------------------------------

/**
 * `data` payload inside the logout response.
 */
data class FsLogoutData(
    @SerializedName("message") val message: String
)

// ---------------------------------------------------------------------------
// Refresh Token  —  POST /api/v1/auth/refresh
// ---------------------------------------------------------------------------

/**
 * `data` payload inside the refresh-token response.
 */
data class FsRefreshTokenData(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("id_token") val idToken: String
)

// ---------------------------------------------------------------------------
// Update Profile  —  PUT /api/v1/auth/profile
// ---------------------------------------------------------------------------

/**
 * `data` payload inside the update-profile response.
 */
data class FsUpdateProfileData(
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: FsUserProfile
)
