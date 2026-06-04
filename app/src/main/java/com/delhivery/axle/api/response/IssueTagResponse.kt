package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class IssueTagResponse(
    @SerializedName("vrn")
    val vrn: String?,

    @SerializedName("barcode")
    val barcode: String?,

    @SerializedName("status")
    val status: String?
)
