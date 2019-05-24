package com.delhivery.orion.ui.tripdetails

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.data.TripHistoryModel
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.data.home.trips.HomeTripsItemData
import com.delhivery.orion.repository.TripsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import javax.inject.Inject

class TripDetailsViewModel @Inject constructor(
  private val tripsRepository: TripsRepository
) : BaseViewModel() {

  /* transaction id */
  lateinit var transactionId: String

  /* trip details live data */
  var tripLiveData =
    MutableLiveData<Triple<HomeBidsRequestItemData, HomeTripsItemData, List<TripHistoryModel>>>()

  /**
   * Fetch trip details
   */
  fun tripDetails() {
    compositeDisposable += tripsRepository.tripDetails(transactionId)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error) {
            tripLiveData.postValue(_res)
          } else {
            error.handle()
          }
        }
  }
}