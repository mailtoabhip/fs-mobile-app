package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class GstDetailRequest (
    @SerializedName("gst_number") val gst_number: String
    )