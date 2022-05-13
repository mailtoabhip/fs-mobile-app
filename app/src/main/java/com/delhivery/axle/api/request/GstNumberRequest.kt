package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class GstNumberRequest (
    @SerializedName("pan_number") val pan_number: String
    )