package com.delhivery.axle.ui.home.activity.fuel

import com.delhivery.axle.repository.TripsRepository
import com.delhivery.axle.repository.WalletRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.bids.TripType
import com.delhivery.axle.ui.bids.TripType.Unknown
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class TripsFuelCardViewModel @Inject constructor(
  private val tripsRepository: TripsRepository,
  private val walletRepository: WalletRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  var hasMoreData = true
  var offset = 0

  var trip: TripType = Unknown

  fun fetchFuelCard() {

  }

  fun fetchTrips(paginate: Boolean) {
  }
}