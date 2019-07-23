package com.delhivery.axle.ui.tripdetails

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.response.TripChargesResponse
import com.delhivery.axle.data.AdvancePaid
import com.delhivery.axle.data.AdvancePending
import com.delhivery.axle.data.AwaitingPODUpload
import com.delhivery.axle.data.AwaitingUnloading
import com.delhivery.axle.data.BalancePending
import com.delhivery.axle.data.InTransit
import com.delhivery.axle.data.PODUploaded
import com.delhivery.axle.data.ReachedDestination
import com.delhivery.axle.data.ReachedPickupPoint
import com.delhivery.axle.data.TripHistoryItem
import com.delhivery.axle.data.TripHistoryModel
import com.delhivery.axle.data.TruckLoaded
import com.delhivery.axle.data.TruckPlaced
import com.delhivery.axle.data.TruckUnloaded
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.TripBidDetails
import com.delhivery.axle.data.home.trips.TripStatus
import com.delhivery.axle.repository.PaymentRepository
import com.delhivery.axle.repository.TripsRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
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

  var balancePaid = false

  var advancePaid = false

  var bidDetail: TripBidDetails? = null

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
                    "₹ ${String.format("%, .0f", tripDetail.bidDetails?.advancePayout)}" +
                        " will be paid once the loading is completed"
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
                  StringUtils.capitalize(
                      history.details?.driverDetails?.driverName ?: "driver"
                  )
                      + " has reached pickup point",
                  history.details?.getArrivalEpoch() ?: ""
              )
          )
        }

        TripStatus.TruckLoaded.statusKey -> {
          tripHistory.add(
              TripHistoryItem(
                  TruckLoaded,
                  "Loading Completed",
                  "Truck is ready to start " + "from ${tripDetail.loadingLocation}",
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
              if (tripDetail.advanceStatus()) {
                advancePaid = true
                tripHistory.add(
                    TripHistoryItem(
                        AdvancePaid,
                        "Advance Paid",
                        "Advance payment of " +
                            "₹ ${String.format("%, .0f", tripDetail.bidDetails?.advancePayout)}" +
                            " has been paid",
                        history.epoch()
                    )
                )
              } else {
                advancePaid = false
                tripHistory.add(
                    TripHistoryItem(
                        AdvancePending,
                        "Advance Pending",
                        "Advance payment of " +
                            "₹ ${String.format("%, .0f", tripDetail.bidDetails?.advancePayout)}" +
                            " has been initiated",
                        history.epoch()
                    )
                )
              }
            } else {
              advancePaid = false
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

        TripStatus.EPodUploaded.statusKey -> {
          tripHistory.add(
              TripHistoryItem(
                  PODUploaded,
                  "POD uploaded",
                  "Balance amount will be settled soon",
                  history.epoch(),
                  history.details?.podUrl ?: ""
              )
          )
        }

        TripStatus.TripCompleted.statusKey -> {
          balancePaid = true
          tripHistory.add(
              TripHistoryItem(
                  BalancePending,
                  "Balance pending",
                  "Invoice will be shared post payment"
              )
          )
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