package com.delhivery.axle.api.repository

import com.delhivery.axle.api.service.TruckService
import com.delhivery.axle.utils.extensions.convertResponse
import com.google.gson.JsonObject
import javax.inject.Inject

class TruckRepository @Inject constructor(
    private val truckService: TruckService
) : BaseRepository() {

    fun getTruckType()=truckService.getTrucks().convertResponse()

    fun getSupplierRewards(jsonObject: JsonObject)=truckService.getSupplierRewards(jsonObject).convertResponse()

}