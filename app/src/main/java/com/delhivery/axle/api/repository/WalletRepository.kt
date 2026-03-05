package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.BankTransferRequest
import com.delhivery.axle.api.request.CreateFuelCardRequest
import com.delhivery.axle.api.request.WalletRechargeReqBody
import com.delhivery.axle.api.request.WalletRechargeRequest
import com.delhivery.axle.api.request.WalletUpdateRequest
import com.delhivery.axle.api.service.WalletService
import com.delhivery.axle.utils.extensions.convertResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wallet Repository
 * -- Manage interaction with wallet service
 *
 */
@Singleton
class WalletRepository @Inject constructor(
  private val walletService: WalletService
) : BaseRepository() {

  /**
   * Activate wallet
   */
  fun activateWallet() = walletService.activateWallet(
      WalletUpdateRequest.getRequest(false)
  ).convertResponse()

  /**
   * Fetches user wallet data
   */
  fun fetchWalletData() = walletService.fetchWalletData().convertResponse()

  /**
   * Fetches wallet transactions
   */
  fun fetchWalletTransactions() = walletService.fetchWalletTransactions().convertResponse()

  /**
   * Tranfer [amount] from wallet to bank
   */
  fun transferToBank(amount: Int) =
    walletService.transferToBank(
        BankTransferRequest.getRequest(amount.toString())
    ).convertResponse()

  /**
   * Create fuel card
   */
  fun createFuelCard(
    mobile: String,
    vehicleNum: String,
    amount: String,
    tripId: String
  ) = walletService.createFuelCard(
      CreateFuelCardRequest.getRequest(mobile, vehicleNum, tripId, vehicleNum, amount)
  ).convertResponse()

    /**
     * TODO :- Remove Later
     * Recharge wallet
     */
    fun rechargeWallet(
        walletId: String,
        amount: Int,
        redirectUrl: String,
        userName: String,
        apiReqId: String,
        authorizationToken: String = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7InNvdXJjZSI6IldFQiIsInVjaWQiOiIwYzUyYzNmMi1lNjE3LTE2OTEtMzM1MC1hMGM3YjQyZDI4NjQifSwiZXhwIjoxNzcyNjE1MDczLCJpYXQiOjE3NzI1Mjg2NzMsImp0aSI6IjY5NWU3YzczLThiY2MtNDMwOS1iNWZlLWE2MTlmY2Q4MTJiMSJ9.Mq5HzkWIvwrxslKbvMyg62uNryCHiafx5HbUU_VeI-I"
    ) = walletService.rechargeWallet(
        walletId,
        authorizationToken,
        apiReqId,
        WalletRechargeRequest.getRequest(amount, redirectUrl, userName)
    )

}