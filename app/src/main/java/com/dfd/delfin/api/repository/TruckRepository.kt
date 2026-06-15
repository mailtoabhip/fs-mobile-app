package com.dfd.delfin.api.repository

import com.dfd.delfin.api.service.TruckService
import com.dfd.delfin.utils.ErrorLogger
import com.dfd.delfin.utils.extensions.convertResponse
import com.google.gson.JsonObject
import javax.inject.Inject

class TruckRepository @Inject constructor(
    private val truckService: TruckService,
    errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {
    fun getSupplierRewards(jsonObject: JsonObject)=truckService.getSupplierRewards(jsonObject).convertResponse()

}