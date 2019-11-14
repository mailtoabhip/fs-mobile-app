package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class OTPSentResponse(@SerializedName("success") val successMsg: String)

data class LoginResponse(@SerializedName("jwt") val jwtToken: String)

data class DelegationTokenResponse(
  @SerializedName("delegation_token") val delegationToken: DelegationToken
)

data class DelegationToken(
  @SerializedName("aws_access_key") val accessKey: String,
  @SerializedName("aws_secret_key") val secretKey: String,
  @SerializedName("session_token") val sessionToken: String
)