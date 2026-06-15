package com.dfd.delfin.ui.trucks

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.TripsRepository
import com.dfd.delfin.api.repository.TruckRepository
import com.dfd.delfin.api.response.TruckResponseArray
import com.dfd.delfin.data.CityModel
import com.dfd.delfin.data.home.trucks.HomeTrucksRequestItemData
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.prefs.UserPrefs
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