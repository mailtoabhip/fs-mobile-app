package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class BankValidationResponse(
  @SerializedName("match_score") val matchScore: String?=null,
  @SerializedName("match_status") val matchStatus: String?=null,
  @SerializedName("manual_verification_required") val manualVerificationRequired: String?=null
)