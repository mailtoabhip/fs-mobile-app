package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.WarehouseRequest
import com.delhivery.axle.api.service.WarehouseService
import com.delhivery.axle.utils.ErrorLogger
import com.delhivery.axle.utils.extensions.convertResponse
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