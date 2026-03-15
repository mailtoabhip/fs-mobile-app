package com.delhivery.axle.api.repository

import com.delhivery.axle.api.service.UtilityService
import com.delhivery.axle.utils.ErrorLogger
import com.delhivery.axle.utils.extensions.convertResponse
import com.google.gson.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UtilityRepository @Inject constructor(
  private val utilityService: UtilityService,
  errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

  /**
   * Get charges
   */
  fun fetchCharges(
    payload: JsonObject
  ) = utilityService.fetchCharges(payload).convertResponse()

}