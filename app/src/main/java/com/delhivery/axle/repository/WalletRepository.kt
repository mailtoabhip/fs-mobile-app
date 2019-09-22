package com.delhivery.axle.repository

import com.auth0.android.jwt.JWT
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.api.WalletService
import com.delhivery.axle.api.request.BankTransferRequest
import com.delhivery.axle.api.request.WalletUpdateRequest
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.prefs.UserPrefs
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
  private val userPrefs: UserPrefs
) : BaseRepository() {

  private fun walletId() =
    when (BuildConfig.FLAVOR) {
      "development" -> "wallet::wallet::fcb31360-7ae4-11e9-9d32-0223f692f646"
      else -> {
        val idSuffix = JWT(userPrefs.jwtToken!!).claims["sub"]?.asString()!!
        "wallet::wallet::${idSuffix.substring(11)}"
      }
    }

  /**
   * Activate wallet
   */
  fun activateWallet() = walletService.activateWallet(
      walletId(), WalletUpdateRequest.getRequest(false)
  ).convertResponse()

  /**
   * Fetches user wallet data
   */
  fun fetchWalletData() = walletService.fetchWalletData(walletId()).convertResponse()

  /**
   * Fetches wallet transactions
   */
  fun fetchWalletTransactions() =
    walletService.fetchWalletTransactions(walletId()).convertResponse()

  /**
   * Tranfer [amount] from wallet to bank
   */
  fun transferToBank(amount: Int) =
    walletService.transferToBank(
        walletId(), BankTransferRequest.getRequest(amount.toString())
    ).convertResponse()

}