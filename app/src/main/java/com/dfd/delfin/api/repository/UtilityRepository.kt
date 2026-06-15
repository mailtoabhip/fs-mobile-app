package com.dfd.delfin.api.repository

import com.dfd.delfin.api.service.UtilityService
import com.dfd.delfin.utils.ErrorLogger
import com.dfd.delfin.utils.extensions.convertResponse
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