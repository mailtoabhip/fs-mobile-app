package com.delhivery.axle.ui.fastag.wallet

import com.delhivery.axle.api.service.WalletApiService
import com.google.gson.JsonObject
import javax.inject.Inject

class AddMoneyRepository @Inject constructor(
    private val walletApiService: WalletApiService
) {

    fun initiateRecharge(amount: Float, redirectUrl: String, clRequestId: String) =
        walletApiService.rechargeWallet(
            JsonObject().apply {
                addProperty("amount", amount)
                addProperty("redirect_url", redirectUrl)
                addProperty("cl_request_id", clRequestId)
            }
        )

    fun checkRechargeStatus(rechargeId: String) =
        walletApiService.fetchRechargeStatus(
            JsonObject().apply {
                addProperty("recharge_id", rechargeId)
            }
        )
}
