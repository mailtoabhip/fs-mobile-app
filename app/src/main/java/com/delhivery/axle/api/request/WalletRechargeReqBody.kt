package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class WalletRechargeReqBody(
    val amount: Int,
    @SerializedName("redirect_url")
    val deeplinkUrl: String
)

/**
 * TODO: Remove it
 * Wallet recharge request creator
 */
data class WalletRechargeRequest(
    @SerializedName("amount") val amount: Int,
    @SerializedName("redirect_url") val redirectUrl: String,
    @SerializedName("user_name") val userName: String
) {
    companion object {
        /**
         * @param amount Recharge amount
         * @param redirectUrl URL to redirect after payment
         * @param userName User name/ID
         */
        fun getRequest(amount: Int, redirectUrl: String, userName: String) =
            WalletRechargeRequest(amount, redirectUrl, userName)
    }
}