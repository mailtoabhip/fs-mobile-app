package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class GstNumberData(
        @SerializedName("pan_number") var pan_number : String?,
        @SerializedName("gstin_numbers") var gstin_numbers : List<String>
)