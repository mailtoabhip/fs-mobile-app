package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.PodRequest
import com.delhivery.axle.api.request.UpdateDispatchRequest
import com.delhivery.axle.api.response.TripSummaryResponse
import com.delhivery.axle.api.service.TransactionService
import com.delhivery.axle.api.service.TripService
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.utils.extensions.convertResponse
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripsRepository @Inject constructor(
  private val userRepository: UserRepository,
  private val tripsService: TripService,
  private val transactionService: TransactionService
) : BaseRepository() {

  /**
   * Complete trip details with transaction and trip history
   */
  fun tripAndTransactionDetails(transactionId: String): Single<Pair<HomeBidsRequestItemData, HomeTripsItemData>> =
    Single.zip(
        transactionService.transactionDetails(transactionId).convertResponse(),
        tripsService.trip(transactionId).convertResponse(),
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
    //imageUrls: MutableList<String>
  ) = tripsService.updateTrip(transactionId, podRequest)

  /**
   * Update tracking details
   */
  fun updateDispatchDetails(
    request: UpdateDispatchRequest
  ) = tripsService.updateDispatchDetails(request).convertResponse()
}

/* User trips pagination load limit */
const val UserTripsLoadLimit = 10