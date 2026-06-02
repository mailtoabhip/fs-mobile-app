package com.delhivery.axle.ui.fastag.wallet

import com.delhivery.axle.api.service.WalletApiService
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonObject
import javax.inject.Inject

class AddMoneyRepository @Inject constructor(
    private val walletApiService: WalletApiService,
    private val userPrefs: UserPrefs
) {

    fun initiateRecharge(amount: Float, redirectUrl: String, clRequestId: String) =
        walletApiService.rechargeWallet(
            userId = userPrefs.userId(),
            request = JsonObject().apply {
                addProperty("amount", amount)
                addProperty("redirect_url", redirectUrl)
                addProperty("cl_request_id", clRequestId)
            }
        )

    fun checkRechargeStatus(rechargeId: String) =
        walletApiService.fetchRechargeStatus(
            userId = userPrefs.userId(),
            request = JsonObject().apply {
                addProperty("recharge_id", rechargeId)
            }
        )
}
