package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class BankValidationResponse(
  @SerializedName("message") val message: String?=null,
  @SerializedName("account_holder_name") val accountHolderName: String?=null,
  @SerializedName("manual_verification_required") val manualVerificationRequired: String?=null,
  @SerializedName("validated") val validated: Boolean?=false
)