package com.dfd.delfin.api.repository

import com.dfd.delfin.api.request.WarehouseRequest
import com.dfd.delfin.api.service.WarehouseService
import com.dfd.delfin.utils.ErrorLogger
import com.dfd.delfin.utils.extensions.convertResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WarehouseRepository @Inject constructor(
  private val warehouseService: WarehouseService,
  errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

  /**
   * Get bulk transactions using ids
   */
  fun fetchWarehouseDetails(
    clientId: String,
    warehouseName: String
  ) = warehouseService.fetchWarehouseDetails(clientId, warehouseName)
      .convertResponse()

  fun getWarehouseDetails(
         warehouseRequest: WarehouseRequest
  ) = warehouseService.postWarehouseDetails(warehouseRequest).convertResponse()

}