package com.delhivery.axle.ui.home.fragments.wallet

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.response.WalletData
import com.delhivery.axle.repository.WalletRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

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
            this.optinDate = _res.wallet.optinDate?:""
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
            this.optinDate = _res.wallet.optinDate?:""
          } else {
            walletLiveData.postValue(null)
          }
        }
  }

}
