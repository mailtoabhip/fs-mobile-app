package com.delhivery.axle.ui.home.activity.fuelcard

import com.delhivery.axle.api.response.WalletData
import com.delhivery.axle.data.fuelcards.FuelCardData
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.repository.WalletRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class CreateFuelCardViewModel @Inject constructor(
  private val walletRepository: WalletRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  lateinit var trip: HomeTripsItemData

  var balance: Int = 0

  var fuelCard: FuelCardData? = null

  lateinit var wallet: WalletData

  /**
   * Create fuel card for [tripId] and [amount]
   */
  fun createFuelCard(
    mobileNum: String,
    vehicleNum: String,
    amount: String,
    tripId: String
  ) {

  }
}