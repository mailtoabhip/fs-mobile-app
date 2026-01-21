package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class FastagBalanceResponse(
    @SerializedName("data")
    val data: FastagBalanceData,

    @SerializedName("status")
    val status: String
)

data class FastagBalanceData(
    @SerializedName("data_time")
    val dataTime: String,

    @SerializedName("fastag_balance")
    val fastagBalance: String
)
