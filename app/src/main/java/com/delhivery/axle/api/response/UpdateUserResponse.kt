package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName


data class UpdateUserResponse(
        @SerializedName("success") val successMsg: String
)
