package com.delhivery.axle.api.repository

import com.delhivery.axle.api.service.TruckService
import com.delhivery.axle.utils.ErrorLogger
import com.delhivery.axle.utils.extensions.convertResponse
import com.google.gson.JsonObject
import javax.inject.Inject

class TruckRepository @Inject constructor(
    private val truckService: TruckService,
    errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {
    fun getSupplierRewards(jsonObject: JsonObject)=truckService.getSupplierRewards(jsonObject).convertResponse()

}