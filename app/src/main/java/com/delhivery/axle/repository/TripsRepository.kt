package com.delhivery.axle.repository

import com.delhivery.axle.api.TransactionService
import com.delhivery.axle.api.TripService
import com.delhivery.axle.api.response.TripSummaryResponse
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.TripStatus
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
   * @status set null to fetch all trips or add any single trip status
   * Get user trips
   *
   */
  fun trips(
    offset: Int = 0,
    status: TripStatus? = null
  ) = tripsService.tripsForStatuses(
      userRepository.userId(), UserTripsLoadLimit,
      offset, status?.statusKey
  )
      .convertResponse()

  /**
   * @status set null to fetch all trips or add any comma separated trip statuses
   * Get user trips for multiple statuses
   *
   */
  fun trips(
    offset: Int = 0,
    statuses: String,
    updatedAfter: String? = null
  ) = tripsService.tripsForStatuses(
      userRepository.userId(), UserTripsLoadLimit,
      offset, statuses, updatedAfter
  )
      .convertResponse()

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
}

/* User trips pagination load limit */
const val UserTripsLoadLimit = 10