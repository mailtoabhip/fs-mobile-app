package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class WalletRechargeStatusResponse(
    val amount: String,
    @SerializedName("recharge_id") val rechargeId: String,
    val status: String
)