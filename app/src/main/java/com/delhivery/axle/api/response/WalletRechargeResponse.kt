package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class WalletRechargeResponse(
    @SerializedName("payment_link")
    val paymentLink: String,
    @SerializedName("recharge_id")
    val rechargeId: String
)