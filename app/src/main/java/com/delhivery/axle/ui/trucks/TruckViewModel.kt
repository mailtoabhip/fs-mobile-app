package com.delhivery.axle.ui.trucks

import com.delhivery.axle.api.repository.TripsRepository
import com.delhivery.axle.api.request.AddVehicle
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class TruckViewModel @Inject constructor(
    val tripsRepository: TripsRepository,
    val userPrefs: UserPrefs
    ) : BaseViewModel(){

    var truckType : String = ""
    var truckSize: String = ""
    var truckCapacity: String = ""
    var truckNumber: String = ""
    var truckCity: CityModel? = null
    var truckDestination : CityModel? = null
    var truckOwnership: String = ""


    fun addNewTruck(){
        val addVehicleRequest = AddVehicle(userPrefs.userId(), userPrefs.userName, truckType, truckNumber, truckOwnership, truckSize,truckCapacity.toDouble()
            ,truckCity!!.city, truckCity!!.orion_db_city_code)

//        compositeDisposable += tripsRepository.tripDetails(232)
//            .onBackground()
//            .subscribe{
//
//            }

    }
}