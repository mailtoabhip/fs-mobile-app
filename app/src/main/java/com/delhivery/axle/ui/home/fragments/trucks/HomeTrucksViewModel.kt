package com.delhivery.axle.ui.home.fragments.trucks

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.InventoryRepository
import com.delhivery.axle.api.repository.TruckRepository
import com.delhivery.axle.api.request.DeactivateTruckRequest
import com.delhivery.axle.api.request.DeleteTruckRequest
import com.delhivery.axle.api.request.UpdateTruck
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.home.loads.HomeLoadsFilterItemData
import com.delhivery.axle.data.home.loads.HomeLoadsSummaryItemData
import com.delhivery.axle.data.home.trucks.HomeTrucksInfoItemData
import com.delhivery.axle.data.home.trucks.HomeTrucksPriorityItemData
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.home.fragments.loads.*
import com.delhivery.axle.ui.trucks.ActivateTruckInterface
import com.delhivery.axle.ui.trucks.EditTruckInterface
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
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
    private val truckRepository: TruckRepository,
    val userPrefs: UserPrefs
): BaseViewModel(), ActivateTruckInterface, EditTruckInterface {

    var bodyTypeFilter: String? =null
    var availabilityFilter: String?= null
    var sizeFilter: String?= null

    //Live data variables

    /* data loading live data */
    var dataLoadingLiveData = MutableLiveData<Boolean>()

    var truckSizeData = mutableListOf<String>()
    var activateTruckLiveData = MutableLiveData<Pair<Int,HomeTrucksRequestItemData>>()
    var editTruckLiveData = MutableLiveData<Pair<Int,HomeTrucksRequestItemData>>()
    var deactivateTruckLiveData = MutableLiveData<Pair<Int,HomeTrucksRequestItemData>>()
    var deleteTruckLiveData = MutableLiveData<Pair<Int,Boolean>>()

    /* user bids live data */
    var userTrucksData =
        MutableLiveData<List<Pair<BaseHomeTrucksRVAdapterItem<*>, DataRVAdapterOperationType>>>()

    fun fetchTruckType() {
        compositeDisposable += truckRepository.getTruckType()
            .onBackground()
            .subscribe { _tRes, error ->
                if(!error && _tRes != null){
                    for( truck in _tRes){
                        truckSizeData.add(truck.truckUuid!!)
                    }
                }
                else{
                    error.handle()
                }
            }
    }

    fun getAllInventories(){
        val jsonObject = JsonObject()
        jsonObject.addProperty("supplier_id", userPrefs.userId())

        if(bodyTypeFilter.isNotNullOrEmpty()){
            bodyTypeFilter?.let { jsonObject.addProperty("truck_type", bodyTypeFilter)}
        }
        if(availabilityFilter.isNotNullOrEmpty()) {
            availabilityFilter?.let { jsonObject.addProperty("availability", availabilityFilter) }
        }
        if(sizeFilter.isNotNullOrEmpty()) {
            sizeFilter?.let { jsonObject.addProperty("truck_size", sizeFilter) }
        }

        dataLoadingLiveData.postValue(true)

        compositeDisposable += inventoryRepository.getInventories(jsonObject)
            .onBackground()
            .progress()
            .subscribe{ _res, error ->
                if(!error && _res != null) {
                    val trucksList :List<HomeTrucksRequestItemData> = _res as List<HomeTrucksRequestItemData>
                    mutableListOf<Pair<BaseHomeTrucksRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        add(Pair(HomeTrucksProgressItem(), DataRVAdapterOperationType.Remove))

                        if(trucksList.isNotEmpty()) {
                            add(Pair(HomeTrucksSearchItem(), DataRVAdapterOperationType.AddUpdate))
                            add(Pair(HomeTrucksFilterItem(), DataRVAdapterOperationType.AddUpdate))
                            add(Pair(HomeTruckPriorityAccessItem(HomeTrucksPriorityItemData()), DataRVAdapterOperationType.Add))
                            add(Pair(HomeTrucksInfoItem(HomeTrucksInfoItemData(trucksList.size)),
                                DataRVAdapterOperationType.AddUpdate
                            ))

                            for (trucks in trucksList) {
                                add(
                                    Pair(
                                        HomeTrucksRequestItem(trucks),
                                        DataRVAdapterOperationType.Add
                                    )
                                )
                            }
                        }else{
                            bodyTypeFilter = null
                            availabilityFilter = null
                            sizeFilter = null
                            add(Pair(HomeTrucksWarningItem_NoTrucks, DataRVAdapterOperationType.AddUpdate))
                        }
                    }.let {
                        userTrucksData.postValue(it)
                    }
                }
                else{
                    mutableListOf<Pair<BaseHomeTrucksRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        /* remove progress item */
                        add(Pair(HomeTrucksProgressItem(), DataRVAdapterOperationType.Remove))
                        /* add api time out item */
                        add(Pair(
                            HomeTrucksWarningItem_NoTrucks,
                            DataRVAdapterOperationType.AddUpdate
                        ))
                    }
                        .let { userTrucksData.postValue(it) }

                }

                dataLoadingLiveData.postValue(false)
            }

    }

    fun deactivateTruck(
        data: HomeTrucksRequestItemData,
        reason: String,
        position: Int
    ){
        val request = DeactivateTruckRequest(data.inventoryId, "not_available","deactivate_truck", reason)
        compositeDisposable += inventoryRepository.deActivateTruck(request)
            .onBackground()
            .progress()
            .subscribe{ _res, error ->
                if(!error && _res!= null){
                    deactivateTruckLiveData.postValue(Pair(position,_res))
                }
                else{
                    error.handle()
                    deactivateTruckLiveData.postValue(null)
                }
            }

    }

    override fun activateTruck(
        data: HomeTrucksRequestItemData,
        inventoryId: String,
        currentCity: CityModel,
        destinationCity: CityModel,
        sourcedAs: String,
        price: Double,
        position: Int) {

        compositeDisposable += inventoryRepository.getOriginDestinationCluster(currentCity.orion_db_city_code!!, destinationCity.orion_db_city_code!!)
            .flatMap { t ->
                val request = UpdateTruck(inventoryId, "activate_truck", currentCity.city, currentCity.orion_db_city_code!!, destinationCity.city,
                    destinationCity.orion_db_city_code!!, sourcedAs, t.first, t.second, price, "Free")
                inventoryRepository.activateTruck(request.getRequest())
            }
            .onBackground()
            .progress()
            .subscribe{_res, error ->
                if(!error && _res != null){
                    activateTruckLiveData.postValue(Pair(position,_res))
                }
                else{
                    error.handle()
                    activateTruckLiveData.postValue(null)
                }
            }

    }

    override fun editTruck(
        data: HomeTrucksRequestItemData,
        currentCity: CityModel,
        destinationCity: CityModel,
        sourcedAs: String,
        price: Double,
        ownership:String,
        position: Int) {

        compositeDisposable += inventoryRepository.getOriginDestinationCluster(currentCity.orion_db_city_code!!, destinationCity.orion_db_city_code!!)
            .flatMap { t ->
                val request = UpdateTruck(data.inventoryId, "update_details", currentCity.city, currentCity.orion_db_city_code!!, destinationCity.city,
                    destinationCity.orion_db_city_code!!, sourcedAs, t.first,t.second, price, ownership = ownership)

                inventoryRepository.editTruck(request.getRequest())
            }
            .onBackground()
            .progress()
            .subscribe{_res, error ->
                if(!error && _res != null){
                    editTruckLiveData.postValue(Pair(position,_res))
                }
                else{
                    error.handle()
                    editTruckLiveData.postValue(null)
                }
            }

    }

    fun deleteTruck(
        data: HomeTrucksRequestItemData,
        position: Int
    ){
        compositeDisposable += inventoryRepository.deleteTruck(DeleteTruckRequest(data.inventoryId))
            .onBackground()
            .progress()
            .subscribe{ _res, error ->
                if(!error && _res!= null){
                    deleteTruckLiveData.postValue(Pair(position,true))
                }
                else{
                    error.handle()
                    deleteTruckLiveData.postValue(null)
                }
            }

    }
}