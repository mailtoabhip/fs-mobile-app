package com.delhivery.axle.ui.trucks

import com.delhivery.axle.api.repository.TripsRepository
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.ui.base.BaseViewModel
import javax.inject.Inject

class TruckViewModel @Inject constructor(
    val tripsRepository: TripsRepository
    ) : BaseViewModel(){

    var truckType : String = ""
    var truckSize: String = ""
    var truckCapacity: String = ""
    var truckNumber: String = ""
    var truckCity: CityModel? = null
    var truckDestination : CityModel? = null
    var truckOwnership: String = ""


    fun addNewTruck(){

    }
}