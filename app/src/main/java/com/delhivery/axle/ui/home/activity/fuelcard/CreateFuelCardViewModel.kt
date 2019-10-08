package com.delhivery.axle.ui.home.activity.fuelcard

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.response.WalletData
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.repository.WalletRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * View model for [CreateFuelCardActivity]
 */
class CreateFuelCardViewModel @Inject constructor(
  private val walletRepository: WalletRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  lateinit var trip: HomeTripsItemData

  var balance: Int = 0

  var walletLiveData = MutableLiveData<WalletData>()

  var transactionLiveData = MutableLiveData<String>()

  /**
   * Fetch wallet data
   */
  fun fetchWalletData() {
    compositeDisposable += walletRepository.fetchWalletData()
        .onBackground()
        .subscribe { _res, error ->
          if (_res != null && !error) {
            walletLiveData.postValue(_res.wallet)
          } else {
            error.handle()
          }
        }
  }

  /**
   * Create fuel card for [tripId] and [amount]
   */
  fun createFuelCard(
    mobileNum: String,
    vehicleNum: String,
    amount: String,
    tripId: String
  ) {
    compositeDisposable += walletRepository.createFuelCard(mobileNum, vehicleNum, amount, tripId)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (_res != null && !error) {
            transactionLiveData.postValue(_res.refNumber)
          } else {
            transactionLiveData.postValue("")
          }
        }
  }
}