package com.delhivery.axle.ui.trucks

import androidx.lifecycle.MutableLiveData
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
    val userPrefs: UserPrefs
    ) : BaseViewModel(){

    var truckType : String = ""
    var truckSize: String = ""
    var truckCapacity: String = ""
    var truckNumber: String = ""
    var truckCity: CityModel? = null
    var truckDestination : CityModel? = null
    var truckOwnership: String = ""
     var truckGetLiveData = MutableLiveData<List<TruckResponseArray>>()


    fun addNewTruck(){
        val addVehicleRequest = AddVehicle(userPrefs.userId(), userPrefs.userName, truckType, truckNumber, truckOwnership, truckSize,truckCapacity
            ,truckCity!!.city, truckCity!!.orion_db_city_code)

//        compositeDisposable += tripsRepository.tripDetails(232)
//            .onBackground()
//            .subscribe{
//
//            }

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