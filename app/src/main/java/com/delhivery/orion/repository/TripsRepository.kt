package com.delhivery.orion.repository

import com.delhivery.orion.api.TripService
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
    offset: Int = 0
  ) = tripsService.trips(
      limit = UserTripsLoadLimit, offset = offset
  )   // todo - use user trips api instead
      .convertResponse()
}

/* User trips pagination load limit */
const val UserTripsLoadLimit = 10