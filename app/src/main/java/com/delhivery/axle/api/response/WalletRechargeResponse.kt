package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class WalletRechargeResponse(
    @SerializedName("payment_link")
    val paymentLink: String,
    @SerializedName("recharge_id")
    val rechargeId: String
)

/**
 * TODO: Remove it
 * Wallet Recharge Response - Outer response structure
 */
data class WalletRechargeResp(
    @SerializedName("data") val data: WalletRechargeDataWrapper? = null,
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("request_id") val requestId: String? = null,
    @SerializedName("error") val error: Any? = null
) {
    /**
     * Get the payment URL from nested structure
     */
    fun getPaymentUrl(): PlodDetails? = data?.data?.plodDetails
}

/**
 * Wallet Recharge Data Wrapper - Inner data structure
 */
data class WalletRechargeDataWrapper(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: WalletRechargeData? = null
)

/**
 * Wallet Recharge Data - Actual recharge data
 */
data class WalletRechargeData(
    @SerializedName("recharge_id") val rechargeId: String? = null,
    @SerializedName("plod_details") val plodDetails: PlodDetails? = null
)

/**
 * Plod Details - Payment link details
 */
data class PlodDetails(
    @SerializedName("link") val link: String? = null
)