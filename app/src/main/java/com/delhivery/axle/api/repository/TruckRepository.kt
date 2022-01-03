package com.delhivery.axle.api.repository

import com.delhivery.axle.api.service.TransactionService
import com.delhivery.axle.api.service.TruckService
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class TruckRepository @Inject constructor(
    private val truckService: TruckService
) : BaseRepository() {

fun getTruckType()=truckService.getTrucks().convertResponse()



}