package com.delhivery.orion.repository

import com.delhivery.orion.api.TransactionService
import com.delhivery.orion.api.TripService
import com.delhivery.orion.data.TripHistoryModel
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.data.home.trips.HomeTripsItemData
import com.delhivery.orion.data.home.trips.TripStatus
import com.delhivery.orion.utils.extensions.convertResponse
import io.reactivex.Single
import io.reactivex.functions.Function3
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripsRepository @Inject constructor(
  private val userRepository: UserRepository,
  private val tripsService: TripService,
  private val transactionService: TransactionService
) : BaseRepository() {

  /**
   * Get user trips
   */
  fun trips(
    offset: Int = 0,
    status: TripStatus? = null
  ) = tripsService.trips(
      "0060w0000028cA4A1K"/*userRepository.userId()*/, limit = UserTripsLoadLimit, offset = offset,
      status = status?.statusKey
  )
      .convertResponse()

  /**
   * Complete trip details with transaction and trip history
   */
  fun tripDetails(transactionId: String) = Single.zip(
      transactionService.transactionDetails(transactionId).convertResponse(),
      tripsService.trip(transactionId).convertResponse(),
      tripsService.tripHistory(transactionId).convertResponse(),
      Function3<HomeBidsRequestItemData, HomeTripsItemData, List<TripHistoryModel>, Triple<HomeBidsRequestItemData, HomeTripsItemData, List<TripHistoryModel>>> { t1, t2, t3 ->
        Triple(t1, t2, t3)
      }
  )

  /**
   * User/supplier trip summary [BidSummaryResponse]
   */
  fun userTripsSummary(
  ) = tripsService
      .userTripsSummary(userRepository.userId())
      .convertResponse()
}

/* User trips pagination load limit */
const val UserTripsLoadLimit = 10