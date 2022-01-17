package com.delhivery.axle.ui.home.fragments.trucks

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.InventoryRepository
import com.delhivery.axle.api.request.DeactivateTruckRequest
import com.delhivery.axle.api.request.DeleteTruckRequest
import com.delhivery.axle.api.request.UpdateTruck
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.trucks.ActivateTruckInterface
import com.delhivery.axle.ui.trucks.EditTruckInterface
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonObject
import javax.inject.Inject

/**
 * View model class for [HomeTrucksFragment]
 */

class HomeTrucksViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    val userPrefs: UserPrefs
): BaseViewModel(), ActivateTruckInterface, EditTruckInterface {

    var bodyTypeFilter: String? =null
    var availabilityFilter: String?= null
    var sizeFilter: String?= null

    //Live data variables
    var activateTruckLiveData = MutableLiveData<Pair<Int,Boolean>>()
    var editTruckLiveData = MutableLiveData<Pair<Int,Boolean>>()
    var deactivateTruckLiveData = MutableLiveData<Pair<Int,Boolean>>()
    var deleteTruckLiveData = MutableLiveData<Pair<Int,Boolean>>()

    fun getAllInventories(){
        val jsonObject = JsonObject()
        jsonObject.addProperty("supplier_id", userPrefs.userId())

        bodyTypeFilter?.let { jsonObject.addProperty("body_type", bodyTypeFilter)}
        availabilityFilter?.let { jsonObject.addProperty("availability", availabilityFilter) }
        sizeFilter?.let { jsonObject.addProperty("truck_size", sizeFilter) }

        compositeDisposable += inventoryRepository.getInventories(jsonObject)
            .onBackground()
            .progress()
            .subscribe{ _res,error ->
                if(!error && _res != null){

                }
                else{

                }
            }

    }

    fun deactivateTruck(
        inventoryId: String,
        reason: String,
        position: Int
    ){
        val request = DeactivateTruckRequest(inventoryId, "not_available","deactivate_truck", reason)
        compositeDisposable += inventoryRepository.deActivateTruck(request)
            .onBackground()
            .progress()
            .subscribe{ _res, error ->
                if(!error && _res!= null){

                }
                else{
                    error.handle()
                    deactivateTruckLiveData.postValue(null)
                }
            }

    }

    override fun activateTruck(
        inventoryId: String,
        currentCity: CityModel,
        destinationCity: CityModel,
        sourcedAs: String,
        price: Double,
        position: Int) {
          val request = UpdateTruck(inventoryId,"activate_truck", currentCity.city, currentCity.orion_db_city_code!!, destinationCity.city,
              destinationCity.orion_db_city_code!!, sourcedAs,"available" )
         compositeDisposable += inventoryRepository.activateTruck(request.getRequest())
            .onBackground()
            .progress()
            .subscribe{_res, error ->
                if(!error && _res != null){

                }
                else{
                    error.handle()
                    activateTruckLiveData.postValue(null)
                }
            }

    }

    override fun editTruck(
        inventoryId: String,
        currentCity: CityModel,
        destinationCity: CityModel,
        sourcedAs: String,
        price: Double,
        position: Int) {

        val request = UpdateTruck(inventoryId,"update_details", currentCity.city, currentCity.orion_db_city_code!!, destinationCity.city,
            destinationCity.orion_db_city_code!!, sourcedAs )

        compositeDisposable += inventoryRepository.editTruck(request.getRequest())
            .onBackground()
            .progress()
            .subscribe{_res, error ->
                if(!error && _res != null){

                }
                else{
                    error.handle()
                    editTruckLiveData.postValue(null)
                }
            }

    }

    fun deleteTruck(
        inventoryId: String,
        position: Int
    ){
        compositeDisposable += inventoryRepository.deleteTruck(DeleteTruckRequest(inventoryId))
            .onBackground()
            .progress()
            .subscribe{ _res, error ->
                if(!error && _res!= null){

                }
                else{
                    error.handle()
                    deleteTruckLiveData.postValue(null)
                }
            }

    }
}