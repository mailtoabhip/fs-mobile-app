package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class FastagLeadResponse(
    @SerializedName("lead_id") val leadId: Int?,
    @SerializedName("message") val message: String?
)
