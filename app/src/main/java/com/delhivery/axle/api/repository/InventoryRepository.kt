package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.DeactivateTruckRequest
import com.delhivery.axle.api.request.DeleteTruckRequest
import com.delhivery.axle.api.service.CityService
import com.delhivery.axle.api.service.InventoryService
import com.delhivery.axle.utils.extensions.convertMessageResponse
import com.delhivery.axle.utils.extensions.convertResponse
import com.google.gson.JsonObject
import javax.inject.Inject

class InventoryRepository @Inject constructor(
    private val inventoryService: InventoryService,
    private val cityService: CityService
): BaseRepository() {

    fun getInventories(request: JsonObject) = inventoryService.getInventories(request).convertResponse()

    fun addInventory(request: JsonObject)= inventoryService.addInventory(request).convertResponse()

    fun activateTruck(request: JsonObject) = inventoryService.activateTruck(request).convertResponse()

    fun editTruck(request: JsonObject) = inventoryService.editTruck(request).convertResponse()

    fun deleteTruck(request: DeleteTruckRequest) = inventoryService.deleteTruck(request).convertMessageResponse()

    fun deActivateTruck(request: DeactivateTruckRequest) = inventoryService.deActivateTruck(request).convertResponse()


}