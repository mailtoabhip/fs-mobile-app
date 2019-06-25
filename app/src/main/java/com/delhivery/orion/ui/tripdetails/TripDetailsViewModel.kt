package com.delhivery.orion.ui.tripdetails

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.api.response.PaymentResponse
import com.delhivery.orion.data.TripHistoryModel
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.data.home.trips.HomeTripsItemData
import com.delhivery.orion.repository.PaymentRepository
import com.delhivery.orion.repository.TripsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import javax.inject.Inject

class TripDetailsViewModel @Inject constructor(
  private val tripsRepository: TripsRepository,
  private val paymentRepository: PaymentRepository
) : BaseViewModel() {

  /* transaction id */
  lateinit var transactionId: String

  /* trip details live data */
  var tripLiveData =
    MutableLiveData<Triple<HomeBidsRequestItemData, HomeTripsItemData, List<TripHistoryModel>>>()

  /* payment summary */
  var paymentSummary = mutableListOf<PaymentResponse>()

  /* trip history */
  var tripHistory = mutableListOf<TripHistoryModel>()

  /**
   * Fetch trip details
   */
  fun fetchTripDetails() {
    compositeDisposable += tripsRepository.tripDetails(transactionId)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error) {
            tripHistory.clear()
            tripHistory.addAll(_res.third)

            tripLiveData.postValue(_res)
          } else {
            error.handle()
          }
        }
  }

  /**
   * Fetch payment summary
   */
  fun fetchPaymentSummary() {
    compositeDisposable += paymentRepository.chargesSummary(transactionId)
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            paymentSummary.clear()
            paymentSummary.addAll(_res)
          } else {
            error.handle()
          }
        }
  }
}