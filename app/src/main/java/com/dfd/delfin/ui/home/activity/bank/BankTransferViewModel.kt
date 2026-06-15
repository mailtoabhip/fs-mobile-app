package com.dfd.delfin.ui.home.activity.bank

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.WalletRepository
import com.dfd.delfin.api.response.WalletData
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

/**
 * View model for [BankTransferActivity]
 */
class BankTransferViewModel @Inject constructor(
  private val walletRepository: WalletRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  var refNumber: String = ""

  val bankname: String? get() = userPrefs.bankName

  lateinit var wallet: WalletData

  var walletLiveData = MutableLiveData<WalletData>()

  var transactionLiveData = MutableLiveData<String>()

  /**
   * Fetch wallet data
   */
  fun fetchWalletData() {
    compositeDisposable +=
      walletRepository.fetchWalletData()
          .onBackground()
          .subscribe { _res, error ->
            if (_res != null && !error) {
              wallet = _res.wallet
              walletLiveData.postValue(wallet)
            } else {
              error.handle()
            }
          }
  }

  /**
   * Transfer [amount] from wallet to bank
   */
  fun transferToBank(amount: Int) {
    compositeDisposable += walletRepository.transferToBank(amount)
        .onBackground()
        .progress()
        .delay(1000, MILLISECONDS)
        .subscribe { _res, error ->
          if (_res != null && !error) {
            this.refNumber = _res.refNumber ?: ""
            transactionLiveData.postValue(this.refNumber)
          } else {
            transactionLiveData.postValue(null)
          }
        }
  }
}