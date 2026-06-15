package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class RcVerificationResponse(
    @SerializedName("manual_verification_required") var manualVerificationRequired: Boolean
)
