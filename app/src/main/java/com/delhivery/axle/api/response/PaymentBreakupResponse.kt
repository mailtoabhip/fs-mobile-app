package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class PaymentBreakupResponse(
    @SerializedName("issuance_fee")
    val issuanceFee: Int,
    @SerializedName("security_deposit")
    val securityDeposit: Int,
    @SerializedName("minimum_recharge")
    val minimumRecharge: Int,
    @SerializedName("platform_fee")
    val platformFee: Int,
    @SerializedName("grand_total")
    val grandTotal: Int,
    @SerializedName("wallet_balance")
    val walletBalance: Int
)
