package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class KycTypesResponse(
    @SerializedName("bank_code")
    val bankCode: String,
    @SerializedName("kyc_types")
    val kycTypes: List<KycTypeItem>
)

data class KycTypeItem(
    @SerializedName("kyc_type")
    val kycType: String
)
