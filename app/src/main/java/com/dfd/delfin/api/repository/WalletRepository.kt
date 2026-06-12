package com.dfd.delfin.api.repository

import com.dfd.delfin.api.request.BankTransferRequest
import com.dfd.delfin.api.request.CreateFuelCardRequest
import com.dfd.delfin.api.request.WalletUpdateRequest
import com.dfd.delfin.api.service.WalletService
import com.dfd.delfin.utils.ErrorLogger
import com.dfd.delfin.utils.extensions.convertResponse
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wallet Repository
 * -- Manage interaction with wallet service
 *
 */
@Singleton
class WalletRepository @Inject constructor(
  private val walletService: WalletService,
  private val userPrefs: UserPrefs,
  errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

  /**
   * Activate wallet
   */
  fun activateWallet() = walletService.activateWallet(
      userId = userPrefs.userId(),
      payload = WalletUpdateRequest.getRequest(false)
  ).convertResponse()

  /**
   * Fetches user wallet data
   */
  fun fetchWalletData() = walletService.fetchWalletData(
      userId = userPrefs.userId()
  ).convertResponse()

  /**
   * Fetches wallet transactions
   */
  fun fetchWalletTransactions() = walletService.fetchWalletTransactions(
      userId = userPrefs.userId()
  ).convertResponse()

  /**
   * Tranfer [amount] from wallet to bank
   */
  fun transferToBank(amount: Int) =
    walletService.transferToBank(
        userId = userPrefs.userId(),
        payload = BankTransferRequest.getRequest(amount.toString())
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
      userId = userPrefs.userId(),
      payload = CreateFuelCardRequest.getRequest(mobile, vehicleNum, tripId, vehicleNum, amount)
  ).convertResponse()

}