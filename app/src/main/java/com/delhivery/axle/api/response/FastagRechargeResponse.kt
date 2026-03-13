package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class FastagRechargeResponse(
    @SerializedName("fastag_id") val fastagId: String?,
    @SerializedName("recharge_amount") val rechargeAmount: Int?,
    @SerializedName("updated_wallet_balance") val updatedWalletBalance: Double?,
    @SerializedName("fastag_balance") val fastagBalance: String?,
    @SerializedName("status") val status: String?
)
