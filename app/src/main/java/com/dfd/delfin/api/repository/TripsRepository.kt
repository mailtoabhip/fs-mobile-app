package com.dfd.delfin.api.repository

import com.dfd.delfin.api.request.AcceptTransactionBidRequest
import com.dfd.delfin.api.request.PodRequest
import com.dfd.delfin.api.request.UpdateDispatchRequest
import com.dfd.delfin.api.response.TripPaymentResponse
import com.dfd.delfin.api.response.TripSummaryResponse
import com.dfd.delfin.api.service.TransactionService
import com.dfd.delfin.api.service.TripService
import com.dfd.delfin.data.home.bids.HomeBidsRequestItemData
import com.dfd.delfin.data.home.trips.HomeTripsItemData
import com.dfd.delfin.utils.ErrorLogger
import com.dfd.delfin.utils.extensions.convertResponse
import com.google.gson.JsonObject
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripsRepository @Inject constructor(
  private val userRepository: UserRepository,
  private val tripsService: TripService,
  private val transactionService: TransactionService,
  errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

  /**
   * Complete trip details with transaction and trip history
   */
  fun tripAndTransactionDetails(transactionId: String): Single<Pair<HomeBidsRequestItemData, HomeTripsItemData>> =
    Single.zip(
        transactionService.transactionDetails(transactionId).convertResponse().subscribeOn(Schedulers.io()),
        tripsService.trip(transactionId).convertResponse().subscribeOn(Schedulers.io()),
        BiFunction<HomeBidsRequestItemData, HomeTripsItemData,
            Pair<HomeBidsRequestItemData, HomeTripsItemData>> { t1, t2 ->
          Pair(t1, t2)
        }
    )

  /**
   * Complete trip details with transaction and trip history
   */
  fun tripDetails(transactionId: String): Single<HomeTripsItemData> =
    tripsService.trip(transactionId).convertResponse()

  /**
   * User/supplier trip summary [TripSummaryResponse]
   */
  fun userTripsSummary() = tripsService.userTripsSummary(userRepository.userId()).convertResponse()

  /**
   * Upload vendor POD
   */
  fun uploadPod(
    transactionId: String,
    podRequest: PodRequest
  ) = tripsService.updateTrip(transactionId, podRequest)

  /**
   * Update tracking details
   */
  fun updateDispatchDetails(
    request: UpdateDispatchRequest
  ) = tripsService.updateDispatchDetails(request).convertResponse()

  /**
   * Get bulk trips payment
   */
  fun bulkPayments(
    trips: List<HomeTripsItemData>,
    request: JsonObject
  ) : Single<Pair<List<HomeTripsItemData>, List<TripPaymentResponse>>> =
    tripsService.fetchTripsPayments(request)
        .convertResponse()
        .map { Pair(trips, it) }

  fun acceptTripBid(
    transactionId: String,
    supplierId: String,
    supplierName: String,
    bidAmount: Int,
    commercialType: String,
    vehicleNumber: String,
    driverPhone:String,
    driverName: String
  ) = AcceptTransactionBidRequest.getRequest(
     transactionId, supplierId,
    supplierName,
    bidAmount, commercialType,vehicleNumber,driverPhone,driverName
  ).let { tripsService.acceptTripBid(it) }



}

/* User trips pagination load limit */
const val UserTripsLoadLimit = 100