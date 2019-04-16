package com.delhivery.orion.repository

import com.delhivery.orion.api.TripService
import com.delhivery.orion.data.home.TripStatus
import com.delhivery.orion.utils.extensions.convertResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripsRepository @Inject constructor(
  private val userRepository: UserRepository,
  private val tripsService: TripService
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
}

/* User trips pagination load limit */
const val UserTripsLoadLimit = 10