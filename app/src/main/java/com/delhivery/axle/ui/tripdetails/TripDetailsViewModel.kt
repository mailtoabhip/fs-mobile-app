package com.delhivery.axle.ui.tripdetails

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.api.response.TripChargesResponse
import com.delhivery.axle.api.response.TripPaymentsResponse
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.data.AdvancePaid
import com.delhivery.axle.data.AdvancePending
import com.delhivery.axle.data.AwaitingPODUpload
import com.delhivery.axle.data.AwaitingUnloading
import com.delhivery.axle.data.BalancePaid
import com.delhivery.axle.data.BalancePending
import com.delhivery.axle.data.InTransit
import com.delhivery.axle.data.InTransitLocation
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
import com.delhivery.axle.repository.UserRepository
import com.delhivery.axle.repository.WarehouseRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.DatePatterns
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.io.File
import javax.inject.Inject

/**
 * View model for [TripDetailsActivity]
 */
class TripDetailsViewModel @Inject constructor(
  private val tripsRepository: TripsRepository,
  private val paymentRepository: PaymentRepository,
  private val warehouseRepository: WarehouseRepository,
  private var userRepository: UserRepository,
  val userPrefs: UserPrefs
) : BaseViewModel() {

  /* transaction id */
  lateinit var transactionId: String

  lateinit var tripDetail: HomeTripsItemData
  private lateinit var warehouse: String

  /* trip details live data */
  var tripLiveData = MutableLiveData<Pair<HomeBidsRequestItemData, HomeTripsItemData>>()
  var historyLiveData = MutableLiveData<Boolean>()
  var warehouseLiveData = MutableLiveData<String>()
  var delegationLiveData = MutableLiveData<Triple<DelegationToken, String, File>>()

  /* payment summary */
  var paymentSummary = mutableListOf<TripChargesResponse>()

  /* trip history */
  var tripHistory = hashMapOf<Int, TripHistoryItem>()

  var balancePaid = false
  var balancePaidTime: String = ""
  var balanceUTR: String = ""
  var advancePaid = false
  var advancePaidTime: String = ""
  var advanceUTR: String = ""
  var bidDetail: TripBidDetails? = null

  /**
   * Fetch trip details
   */
  fun fetchTripDetails() {
    compositeDisposable += tripsRepository.tripAndTransactionDetails(transactionId)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error) {
            this.tripDetail = _res.second
            this.warehouse = _res.first.pickupLocation
            tripLiveData.postValue(_res)
          } else {
            tripLiveData.postValue(null)
          }
        }
  }

  /**
   * Fetch warehouse details
   */
  fun fetchWarehouseDetails() {
    compositeDisposable += warehouseRepository.fetchWarehouseDetails(
        tripDetail.clientId, warehouse
    )
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            if (_res.warehouses.isNullOrEmpty()) {
              warehouseLiveData.postValue(tripDetail.unloadingLocation)
            } else {
              warehouseLiveData.postValue(_res.warehouses[0].completeAddress())
            }
          }
        }
  }

  /**
   * Fetch history, charges and payments summary
   */
  fun fetchPaymentSummary() {
    compositeDisposable += paymentRepository.historyAndPayments(transactionId)
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            processTrips(_res.first, _res.second)
            historyLiveData.postValue(true)
          } else {
            error.handle()
          }
        }
  }

  /**
   * Fetch history And Payments summary
   */
  fun fetchPayments() {
    compositeDisposable += paymentRepository.historyAndPayments(transactionId)
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            processTrips(_res.first, _res.second)
            historyLiveData.postValue(true)
          } else {
            error.handle()
          }
        }
  }

  /**
   * Fetch charges summary
   */
  fun fetchChargesSummary() {
    compositeDisposable += paymentRepository.historyCharges(transactionId)
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            paymentSummary.clear()
            paymentSummary.addAll(_res)
          }
        }
  }

  private fun processTrips(
    histories: List<TripHistoryModel>,
    payments: List<TripPaymentsResponse>
  ) {
    for ((index, history) in histories.withIndex()) {
      when (history.status().statusKey) {
        TripStatus.TruckConfirmed.statusKey -> {
          if (!tripHistory.contains(TruckPlaced)) {
            tripHistory[TruckPlaced] = TripHistoryItem(
                TruckPlaced,
                "Truck Placed",
                "Truck is on its way to pickup location", history.timeStamp()
            )
          }

          if (index == 0 && tripDetail.bidDetails?.advancePayout ?: 0.0 > 0.0
              && !tripHistory.contains(AdvancePending)
          ) {
            tripHistory[AdvancePending] = TripHistoryItem(
                AdvancePending,
                "Advance Pending",
                "₹ ${String.format(
                    "%, .0f",
                    (tripDetail.bidDetails?.advancePayout ?: 0)
                )}" + " will be paid once the loading is completed"
            )
          }
        }

        TripStatus.TruckArrived.statusKey -> {
          if (!tripHistory.contains(ReachedPickupPoint)) {
            tripHistory[ReachedPickupPoint] = TripHistoryItem(
                ReachedPickupPoint,
                "Reached Pickup Point",
                "Driver has reached pickup point",
                history.details?.getArrivalEpoch() ?: ""
            )
          }
        }

        TripStatus.TruckLoaded.statusKey -> {
          if (!tripHistory.contains(TruckLoaded)) {
            tripHistory[TruckLoaded] = TripHistoryItem(
                TruckLoaded,
                "Loading Completed",
                "Truck is ready to start from pickup warehouse",
                history.timeStamp()
            )
          }
        }

        TripStatus.In_Transit.statusKey -> {
          if (TextUtils.isEmpty(history.details?.currentLocation)) {
            if (!tripHistory.contains(InTransit)) {
              tripHistory[InTransit] = TripHistoryItem(
                  InTransit,
                  "In-Transit",
                  "Truck started from pickup location",
                  history.timeStamp()
              )
            }

            if (tripDetail.bidDetails?.advancePayout ?: 0.0 > 0.0) {
              val advancePay = payments.firstOrNull { it.head == "cash_advance" }
              if (advancePay != null) {
                advancePaid = true
                val utrString = when {
                  advancePay.bankTransactionId.isNotNullOrEmpty() -> {
                    advanceUTR = "UTR No: ${advancePay.bankTransactionId}"
                    " with UTR no: ${advancePay.bankTransactionId}."
                  }
                  else -> {
                    ""
                  }
                }

                if (!tripHistory.contains(AdvancePaid)) {
                  tripHistory.remove(AdvancePending)
                  tripHistory[AdvancePaid] = TripHistoryItem(
                      AdvancePaid,
                      "Advance Paid",
                      "Advance payment of ₹ ${String.format(
                          "%, .0f", (tripDetail.bidDetails?.advancePayout ?: 0)
                      )} has been paid$utrString",
                      history.timeStamp()
                  )
                  advancePaidTime = DateUtils.formatDate(
                      DateUtils.parseDate(advancePay.updationTime, DatePatterns.OrionDateFormat),
                      DatePatterns.SimpleDateFormat
                  )
                }
              } else {
                advancePaid = false
                if (!tripHistory.contains(AdvancePending)) {
                  tripHistory[AdvancePending] = TripHistoryItem(
                      AdvancePending,
                      "Advance Pending",
                      "Advance payment of " +
                          "₹ ${String.format(
                              "%, .0f", (tripDetail.bidDetails?.advancePayout ?: 0)
                          )}" +
                          " has been initiated"
                  )
                }
              }
            } else {
              advancePaid = false
              if (!tripHistory.contains(AdvancePending)) {
                tripHistory[AdvancePending] = TripHistoryItem(
                    AdvancePending,
                    "Advance Pending",
                    "Advance payment is being processed, will update shortly"
                )
              }
            }
          } else {
            tripHistory[InTransitLocation] = TripHistoryItem(
                InTransitLocation,
                "In-Transit",
                "Truck is in-transit, current location is ${history.details?.currentLocation}",
                history.timeStamp()
            )
          }
        }

        TripStatus.TruckReached.statusKey -> {
          if (index == 0) {
            if (!tripHistory.contains(AwaitingUnloading)) {
              tripHistory[AwaitingUnloading] = TripHistoryItem(
                  AwaitingUnloading,
                  "Awaiting unloading",
                  "Upload ePOD once truck is unloaded",
                  history.details?.getReachedEpoch() ?: ""
              )
            }
          }


          if (!tripHistory.contains(ReachedDestination)) {
            tripHistory[ReachedDestination] = TripHistoryItem(
                ReachedDestination,
                "Reached Destination",
                "Truck reached the destination",
                history.details?.getReachedEpoch() ?: ""
            )
          }
        }

        TripStatus.TruckUnloaded.statusKey -> {
          if (TextUtils.isEmpty(tripDetail.podUrl) &&
              !tripHistory.contains(AwaitingPODUpload)
          ) {
            tripHistory[AwaitingPODUpload] = TripHistoryItem(
                AwaitingPODUpload,
                "Awaiting POD upload",
                "Balance will be paid within 3 days of Physical POD verification"
            )
          } else if (!tripHistory.contains(PODUploaded) && tripDetail.podUrl.isNotNullOrEmpty()) {
            tripHistory[PODUploaded] = TripHistoryItem(
                PODUploaded,
                "POD uploaded",
                "Balance amount will be settled soon",
                history.timeStamp()
            )
          }

          if (!tripHistory.contains(TruckUnloaded)) {
            tripHistory[TruckUnloaded] = TripHistoryItem(
                TruckUnloaded,
                "Truck Unloaded",
                "Trip has been marked complete",
                history.details?.getUnloadedEpoch() ?: ""
            )
          }
        }

        TripStatus.EPodUploaded.statusKey -> {
          if (!tripHistory.contains(PODUploaded)) {
            tripHistory[PODUploaded] = TripHistoryItem(
                PODUploaded,
                "Awaiting Physical POD",
                "Balance will be paid within 3 days of Physical POD verification",
                history.timeStamp()
            )
          }
        }

        TripStatus.TripCompleted.statusKey -> {
          val balancePay = payments.firstOrNull { it.head == "balance_payment" }

          if (balancePay != null) {
            balancePaid = true
            val utrString = when {
              balancePay.bankTransactionId.isNotNullOrEmpty() -> {
                balanceUTR = "UTR no: ${balancePay.bankTransactionId}"
                " with UTR no: ${balancePay.bankTransactionId}."
              }
              else -> {
                ""
              }
            }
            if (!tripHistory.contains(BalancePaid)) {
              tripHistory.remove(BalancePending)
              tripHistory[BalancePaid] = TripHistoryItem(
                  BalancePaid,
                  "Balance Paid",
                  "Balance payment of ₹ ${String.format(
                      "%, .0f", balancePay.amount
                  )} has been paid$utrString",
                  balancePay.timeStamp()
              )
              balancePaidTime = DateUtils.formatDate(
                  DateUtils.parseDate(balancePay.updationTime, DatePatterns.OrionDateFormat),
                  DatePatterns.SimpleDateFormat
              )
            }
          } else {
            balancePaid = false
            if (!tripHistory.contains(BalancePending)) {
              tripHistory[BalancePending] = TripHistoryItem(
                  BalancePending,
                  "Balance pending",
                  "Invoice will be shared post payment"
              )
            }
          }

          if (TextUtils.isEmpty(tripDetail.podUrl) &&
              !tripHistory.contains(AwaitingPODUpload)
          ) {
            tripHistory[AwaitingPODUpload] = TripHistoryItem(
                AwaitingPODUpload,
                "Awaiting POD upload",
                "Balance will be paid within 3 days of Physical POD verification"
            )
          } else {
            if (!tripHistory.contains(PODUploaded) && tripDetail.podUrl.isNotNullOrEmpty()) {
              tripHistory[PODUploaded] = TripHistoryItem(
                  PODUploaded,
                  "POD uploaded",
                  "Balance amount will be settled soon",
                  history.timeStamp()
              )
            }
          }
        }
      }
    }
  }

  /**
   * Get delegation token for AWS
   */
  fun getDelegationToken(
    awsPath: String,
    file: File
  ) {
    compositeDisposable += userRepository.getDelegationToken(AWSConfig.Target.value())
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            delegationLiveData.postValue(Triple(_res.delegationToken, awsPath, file))
          } else
            error.handle()
        }
  }
}