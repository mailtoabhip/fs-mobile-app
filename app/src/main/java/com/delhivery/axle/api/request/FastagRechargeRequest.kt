package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class FastagRechargeRequest(
    @SerializedName("fastag_id") val fastagId: String,
    @SerializedName("recharge_amount") val rechargeAmount: Int
)
