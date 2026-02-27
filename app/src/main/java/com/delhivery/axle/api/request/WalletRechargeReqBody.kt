package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class WalletRechargeReqBody(
    val amount: Int,
    @SerializedName("redirect_url")
    val deeplinkUrl: String
)