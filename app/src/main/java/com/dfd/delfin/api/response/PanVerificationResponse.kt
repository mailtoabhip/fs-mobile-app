package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName


data class PanVerificationResponse(
    @SerializedName("pan_card") var panCard: String?,
    @SerializedName("pan_holder_name") var panHolderName: String?,
    @SerializedName("pan_card_type") var panCardType: String?
)
