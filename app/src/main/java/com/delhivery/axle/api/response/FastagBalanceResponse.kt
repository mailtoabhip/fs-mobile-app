package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class FastagBalanceResponse(
    @SerializedName("data_time")
    val dataTime: String,

    @SerializedName("fastag_balance")
    val fastagBalance: String
)
