package com.delhivery.axle.api.repository

import com.delhivery.axle.api.service.LoadCycleService
import com.delhivery.axle.utils.extensions.convertResponse
import com.google.gson.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles city service
 */
@Singleton
class LoadCycleRepository @Inject constructor(
  private val loadsService: LoadCycleService
) : BaseRepository() {

  /**
   * Search trips basis params
   */
  fun searchTrips(request: JsonObject) = loadsService.searchTrips(request).convertResponse()

}

/* User trips pagination load limit */
const val UserSearchLimit = 5