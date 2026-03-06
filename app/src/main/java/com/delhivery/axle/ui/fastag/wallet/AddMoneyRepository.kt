package com.delhivery.axle.ui.fastag.wallet

import com.delhivery.axle.api.request.WalletRechargeReqBody
import com.delhivery.axle.api.service.LoadBoardService
import javax.inject.Inject

class AddMoneyRepository @Inject constructor(
    val loadBoardService: LoadBoardService
){

    fun initiateRecharge(walletRechargeReqBody: WalletRechargeReqBody) =
        loadBoardService.initiateRecharge(walletRechargeReqBody)

    fun checkTransactionStatus(transactionId: String, startDate : String) =
        loadBoardService.checkRechargeStatus(
            transactionId, startDate
        )
}