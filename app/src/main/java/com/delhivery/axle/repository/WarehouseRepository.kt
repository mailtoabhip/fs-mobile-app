package com.delhivery.axle.repository

import com.delhivery.axle.api.WarehouseService
import com.delhivery.axle.utils.extensions.convertResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WarehouseRepository @Inject constructor(
  private val warehouseService: WarehouseService
) : BaseRepository() {

  /**
   * Get bulk transactions using ids
   */
  fun fetchWarehouseDetails(
    clientId: String,
    warehouseName: String
  ) = warehouseService.fetchWarehouseDetails(clientId, warehouseName)
      .convertResponse()

}