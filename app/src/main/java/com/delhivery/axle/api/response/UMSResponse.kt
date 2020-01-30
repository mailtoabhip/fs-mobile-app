package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

/**
 * Response for send OTP
 */
data class OTPSentResponse(@SerializedName("success") val successMsg: String)

/**
 * Response for login with OTP
 */
data class LoginResponse(@SerializedName("jwt") val jwtToken: String)

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
