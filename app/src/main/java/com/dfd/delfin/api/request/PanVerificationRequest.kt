package com.dfd.delfin.api.request

import com.google.gson.annotations.SerializedName

data class PanVerificationRequest (
    @SerializedName("pan_number") var panCardNumber: String
    )