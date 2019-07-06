package com.delhivery.orion.ui.tripdetails

import android.arch.lifecycle.MutableLiveData
import android.text.TextUtils
import com.delhivery.orion.api.response.TripChargesResponse
import com.delhivery.orion.data.AdvancePaid
import com.delhivery.orion.data.AdvancePending
import com.delhivery.orion.data.AwaitingPODUpload
import com.delhivery.orion.data.AwaitingUnloading
import com.delhivery.orion.data.BalancePending
import com.delhivery.orion.data.InTransit
import com.delhivery.orion.data.PODUploaded
import com.delhivery.orion.data.ReachedDestination
import com.delhivery.orion.data.ReachedPickupPoint
import com.delhivery.orion.data.TripHistoryItem
import com.delhivery.orion.data.TripHistoryModel
import com.delhivery.orion.data.TruckLoaded
import com.delhivery.orion.data.TruckPlaced
import com.delhivery.orion.data.TruckUnloaded
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.data.home.trips.HomeTripsItemData
import com.delhivery.orion.data.home.trips.TripStatus
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
  var paymentSummary = mutableListOf<TripChargesResponse>()

  /* trip history */
  var tripHistory = mutableListOf<TripHistoryItem>()

  /**
   * Fetch trip details
   */
  fun fetchTripDetails() {
    compositeDisposable += tripsRepository.tripDetails(transactionId)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error) {
            processTrips(_res.third, _res.second)
            tripLiveData.postValue(_res)
          } else {
            error.handle()
          }
        }
  }

  private fun processTrips(
    histories: List<TripHistoryModel>,
    tripDetail: HomeTripsItemData
  ) {
    var index = 0;
    for (history in histories) {
      when (history.status().statusKey) {
        TripStatus.TruckConfirmed.statusKey -> {
          if (index == 0 && tripDetail.bidDetails?.advancePayout ?: 0.0 > 0.0) {
            tripHistory.add(
                TripHistoryItem(
                    AdvancePending,
                    "Advance Pending",
                    "₹ ${tripDetail.bidDetails?.advancePayout} will be paid once the loading is completed"
                )
            )
          }

          tripHistory.add(
              TripHistoryItem(
                  TruckPlaced,
                  "Truck Placed",
                  "Truck is on its way to pickup location", history.epoch()
              )
          )
        }

        TripStatus.TruckArrived.statusKey -> {
          tripHistory.add(
              TripHistoryItem(
                  ReachedPickupPoint,
                  "Reached Pickup Point",
                  (history.details?.driverDetails?.driverName
                      ?: "Driver") + " has reached pickup point",
                  history.details?.getArrivalEpoch() ?: ""
              )
          )
        }

        TripStatus.TruckLoaded.statusKey -> {
          tripHistory.add(
              TripHistoryItem(
                  TruckLoaded,
                  "Loading Completed",
                  "Truck is ready to start to ${history.details?.unloadingLocation}",
                  history.epoch()
              )
          )
        }

        TripStatus.In_Transit.statusKey -> {
          if (TextUtils.isEmpty(history.details?.currentLocation)) {
            tripHistory.add(
                TripHistoryItem(
                    InTransit,
                    "In-Transit",
                    "Truck started from pickup location",
                    history.epoch()
                )
            )

            if (tripDetail.bidDetails?.advancePayout ?: 0.0 > 0.0) {
              tripHistory.add(
                  TripHistoryItem(
                      AdvancePaid,
                      "Advance Paid",
                      "Advance payment of ₹ ${tripDetail.bidDetails?.advancePayout} has been initiated",
                      history.epoch()
                  )
              )
            } else {
              tripHistory.add(
                  TripHistoryItem(
                      AdvancePending,
                      "Advance Pending",
                      "Advance payment is being processed, will update shortly",
                      history.epoch()
                  )
              )
            }
          } else {
            tripHistory.add(
                TripHistoryItem(
                    InTransit,
                    "In-Transit",
                    "Truck is in-transit, current location is  ${history.details?.currentLocation}",
                    history.epoch()
                )
            )
          }
        }

        TripStatus.TruckReached.statusKey -> {
          if (index == 0) {
            tripHistory.add(
                TripHistoryItem(
                    AwaitingUnloading,
                    "Awaiting unloading",
                    "POD will be uploaded once the truck is unloaded",
                    history.details?.getReachedEpoch() ?: ""
                )
            )
          }

          tripHistory.add(
              TripHistoryItem(
                  ReachedDestination,
                  "Reached Destination",
                  "Truck reached the destination",
                  history.details?.getReachedEpoch() ?: ""
              )
          )
        }

        TripStatus.TruckUnloaded.statusKey -> {
          if (index == 0) {
            tripHistory.add(
                TripHistoryItem(
                    AwaitingPODUpload,
                    "Awaiting POD upload",
                    "This can take some days, Please be patient"
                )
            )
          }

          tripHistory.add(
              TripHistoryItem(
                  TruckUnloaded,
                  "Truck Unloaded",
                  "Trip has been marked complete",
                  history.details?.getUnloadedEpoch() ?: ""
              )
          )
        }

        TripStatus.TripCompleted.statusKey -> {
          if (!TextUtils.isEmpty(history.details?.podUrl)) {
            tripHistory.add(
                TripHistoryItem(
                    BalancePending,
                    "Balance pending",
                    "Invoice will be shared post payment"
                )
            )

            tripHistory.add(
                TripHistoryItem(
                    PODUploaded,
                    "POD uploaded",
                    "Balance amount will be settled soon",
                    history.epoch(),
                    history.details?.podUrl ?: ""
                )
            )

          } else {
            tripHistory.add(
                TripHistoryItem(
                    AwaitingPODUpload,
                    "Awaiting POD upload",
                    "This can take some days, Please be patient"
                )
            )
          }
        }
      }

      index++
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
          }
        }
  }
}