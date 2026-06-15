package com.dfd.delfin.ui.home.fragments.wallet

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.WalletRepository
import com.dfd.delfin.api.response.WalletData
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * View model for [HomeWalletFragment]
 */
class HomeWalletViewModel @Inject constructor(
  private val walletRepository: WalletRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  var walletLiveData = MutableLiveData<WalletData>()

  var optinDate = ""

  /**
   * Returns wallet active flag from cache
   */
  var walletActivated: Boolean
    get() = userPrefs.walletActivated
    set(value) {
      userPrefs.walletActivated = value
    }

  /**
   * Activate wallet
   */
  fun activateWallet() {
    compositeDisposable += walletRepository.activateWallet()
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (_res != null && !error) {
            userPrefs.walletActivated = true
            this.optinDate = _res.wallet.optinDate ?: ""
            walletLiveData.postValue(_res.wallet)
          } else {
            walletLiveData.postValue(null)
          }
        }
  }

  /**
   * Fetch wallet data
   */
  fun fetchWalletData() {
    compositeDisposable += walletRepository.fetchWalletData()
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (_res != null && !error) {
            walletLiveData.postValue(_res.wallet)
            this.optinDate = _res.wallet.optinDate ?: ""
          } else {
            walletLiveData.postValue(null)
          }
        }
  }

}
