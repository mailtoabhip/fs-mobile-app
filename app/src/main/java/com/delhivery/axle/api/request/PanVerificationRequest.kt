package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class PanVerificationRequest (
    @SerializedName("pan_card_number") var panCardNumber: String?
    )