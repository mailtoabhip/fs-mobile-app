package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

/**
 * Response for send OTP
 */
data class OTPSentResponse(@SerializedName("success") val successMsg: String,
                           @SerializedName("is_new_user") val isNewUser: Boolean)

/**
 * Response for login with OTP
 */
data class LoginResponse(@SerializedName("jwt_token") val jwtToken: String)


/**
 * Delegation token response
 */
data class DelegationTokenResponse(
  @SerializedName("delegation_token") val delegationToken: DelegationToken
)

/**
 * Delegation token data
 */
data class DelegationToken(
  @SerializedName("aws_access_key") val accessKey: String,
  @SerializedName("aws_secret_key") val secretKey: String,
  @SerializedName("session_token") val sessionToken: String
)

/**
 * Roles and permission response
 */
data class UMSRolePermissionResponse(
  @SerializedName("roles") val roles: List<Role>,
  @SerializedName("permissions") val permissions: List<Permissions>,
  @SerializedName("error") val error: String
)

/**
 * Permission data
 */
data class Permissions(
  @SerializedName("app_id") val appId: String,
  @SerializedName("name") val name: String
)

/**
 * Role data
 */
data class Role(
  @SerializedName("app_id") val appId: String,
  @SerializedName("name") val name: String
)

/**
 * KYC docs
 */
data class KycDocsResponse(
        @SerializedName("kyc_documents") val kyc_documents: List<String>?
)

