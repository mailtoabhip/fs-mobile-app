package com.dfd.delfin.api.request

import com.google.gson.annotations.SerializedName

data class FastagRechargeRequest(
    @SerializedName("fastag_id") val fastagId: String?,
    @SerializedName("recharge_amount") val rechargeAmount: Double?,
    @SerializedName("fastag_status") val fastagStatus: String?,
    @SerializedName("fastag_balance") val fastagBalance: String?
)
