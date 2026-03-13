package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class WalletRechargeData(
    @SerializedName("plod_details") val plodDetails: PlodDetails?,
    @SerializedName("recharge_id") val rechargeId: String?
)

data class PlodDetails(
    @SerializedName("link") val link: String?
)