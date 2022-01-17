package com.delhivery.axle.api.repository

import com.delhivery.axle.api.service.InventoryService
import com.delhivery.axle.utils.extensions.convertResponse
import javax.inject.Inject

class InventoryRepository @Inject constructor(
    private val inventoryService: InventoryService
): BaseRepository() {

    fun addInventory()= inventoryService.addInventory().convertResponse()
}