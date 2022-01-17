package com.delhivery.axle.ui.trucks

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.InventoryRepository
import com.delhivery.axle.api.repository.TripsRepository
import com.delhivery.axle.api.repository.TruckRepository
import com.delhivery.axle.api.request.AddVehicle
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class TruckViewModel @Inject constructor(
    val tripsRepository: TripsRepository,
    val truckRepository: TruckRepository,
    val inventoryRepository: InventoryRepository,
    val userPrefs: UserPrefs
    ) : BaseViewModel(){

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


    fun addNewTruck(sourcedAs: String){
        val addVehicleRequest = AddVehicle(userPrefs.userId(), userPrefs.userName, truckType, truckNumber, truckOwnership, truckSize,truckCapacity
            ,truckCity!!.city, truckCity!!.orion_db_city_code!!, truckDestination!!.city,truckDestination!!.orion_db_city_code!!,sourcedAs)

        compositeDisposable += inventoryRepository.addInventory(addVehicleRequest.getRequest())
            .onBackground()
            .subscribe{_res,error ->
                if(!error && _res!= null){
                    addTruckLiveData.postValue(true)
                }
                else{
                    error.handle()
                    addTruckLiveData.postValue(null)
                }

            }

    }

    fun fetchTruckType() {
        compositeDisposable += truckRepository.getTruckType()
            .onBackground()
            .subscribe { _tRes, error ->
                if(!error && _tRes != null){
                    truckGetLiveData.postValue(_tRes)
                }
                else{
                    error.handle()
                    truckGetLiveData.postValue(null)
                }
            }
    }
}