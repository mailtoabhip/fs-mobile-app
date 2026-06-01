package com.delhivery.axle.ui.trucks

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.TripsRepository
import com.delhivery.axle.api.repository.TruckRepository
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class TruckViewModel @Inject constructor(
    val tripsRepository: TripsRepository,
    val truckRepository: TruckRepository,
    val userPrefs: UserPrefs
    ) : BaseViewModel(){

    //Intent params
    var truckTypeIntent : String = ""
    var truckSizeIntent: String = ""
    var truckCapacityIntent: Double = 0.0
    var minCapIntent: Double =0.0
    var maxCapIntent :Double =0.0
    var sourcedAsIntent: String = ""
    var fromLinks:Boolean = false
    var vehicleNumberIntent: String = ""
    var addTruckSourceIntent: String = ""

    var truckType : String = ""
    var truckSize: String = ""
    var truckCapacity: Double = 0.0
    var truckNumber: String = ""
    var truckCity: CityModel? = null
    var truckDestination : CityModel? = null
    var truckOwnership: String = ""
     var truckGetLiveData = MutableLiveData<List<TruckResponseArray>>()
    var truckPrice: Double = 0.0

    //Live Data variables
    var addTruckLiveData = MutableLiveData<Boolean>()
    var addTruckLiveDataRes = MutableLiveData<HomeTrucksRequestItemData>()


    var noCityCodeError =  MutableLiveData<Boolean>()

    var inventoryLiveData = MutableLiveData<HomeTrucksRequestItemData>()

    var spSecondaryId:String?=null
    var spSecondaryName:String?=null

    //var for maintaining the api vehicle update response<boolean> in placements details activity from add truck dialog
    var isVehicleUpdated : Boolean = false
}