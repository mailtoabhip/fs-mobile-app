package com.dfd.delfin.api.repository

import com.dfd.delfin.api.service.LoadCycleService
import com.dfd.delfin.utils.ErrorLogger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for load cycle operations.
 * 
 * This repository has been migrated to use Flow-based architecture with suspend functions.
 * The Flow-based methods wrap suspend API calls using safeApiCallFlow utility for
 * consistent error handling and Resource state emissions.
 */
@Singleton
class LoadCycleRepository @Inject constructor(
  private val loadsService: LoadCycleService,
  errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

  /**
   * Search trips using Flow-based architecture.
   * 
   * This method wraps the suspend API call in Flow with Resource states:
   * - Resource.Loading: Emitted immediately before API call
   * - Resource.Success: Emitted when API returns data successfully
   * - Resource.Failure: Emitted when API call fails with error details
   *
   * @param request JsonObject containing search parameters (origin, destination, vehicle type, etc.)
   * @return Flow<Resource<SearchTripsResponse>> that emits Loading, then Success or Failure
   */

  /**
   * Get frequent lanes using Flow-based architecture.
   * 
   * This method wraps the suspend API call in Flow with Resource states:
   * - Resource.Loading: Emitted immediately before API call
   * - Resource.Success: Emitted when API returns data successfully
   * - Resource.Failure: Emitted when API call fails with error details
   *
   * @param request JsonObject containing request parameters
   * @return Flow<Resource<FrequentTripsResponse>> that emits Loading, then Success or Failure
   */


  /**
   * Search trips basis params
   */

}

/* User trips pagination load limit */
const val UserSearchLimit = 50