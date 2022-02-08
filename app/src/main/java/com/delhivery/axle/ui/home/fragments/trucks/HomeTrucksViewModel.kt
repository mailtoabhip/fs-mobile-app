package com.delhivery.axle.ui.home.fragments.trucks

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.InventoryRepository
import com.delhivery.axle.api.repository.TruckRepository
import com.delhivery.axle.api.repository.UserTrucksLoadLimit
import com.delhivery.axle.api.request.DeactivateTruckRequest
import com.delhivery.axle.api.request.DeleteTruckRequest
import com.delhivery.axle.api.request.UpdateTruck
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.home.trucks.HomeTrucksInfoItemData
import com.delhivery.axle.data.home.trucks.HomeTrucksPriorityItemData
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.trucks.ActivateTruckInterface
import com.delhivery.axle.ui.trucks.EditTruckInterface
import com.delhivery.axle.utils.UiUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonObject
import retrofit2.HttpException
import javax.inject.Inject

/**
 * View model class for [HomeTrucksFragment]
 */

class HomeTrucksViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val truckRepository: TruckRepository,
    val userPrefs: UserPrefs
): BaseViewModel(), ActivateTruckInterface, EditTruckInterface {

    var bodyTypeFilter = mutableListOf<Pair<String, String>>()
    var availabilityFilter = mutableListOf<Pair<String, String>>()
    var sizeFilter: String?= null

    var hasMoreData = true
    var offset = 0
    var total = 0
    var paginateCount =0

    var searchPrefix = ""
    var searchFlag = false

    //Live data variables

    /* data loading live data */
    var dataLoadingLiveData = MutableLiveData<Boolean>()

    var truckSizeData = mutableListOf<TruckResponseArray>()
    var activateTruckLiveData = MutableLiveData<Pair<Int,HomeTrucksRequestItemData>>()
    var editTruckLiveData = MutableLiveData<Pair<Int,HomeTrucksRequestItemData>>()
    var deactivateTruckLiveData = MutableLiveData<Pair<Int,HomeTrucksRequestItemData>>()
    var deleteTruckLiveData = MutableLiveData<Pair<Int,Boolean>>()

    /* user bids live data */
    var userTrucksData =
        MutableLiveData<List<Pair<BaseHomeTrucksRVAdapterItem<*>, DataRVAdapterOperationType>>>()

    var noCityCodeError =  MutableLiveData<Boolean>()

    fun fetchTruckType() {
        compositeDisposable += truckRepository.getTruckType()
            .onBackground()
            .subscribe { _tRes, error ->
                if(!error && _tRes != null){
                    truckSizeData.addAll(_tRes)
                }
                else{
                    error.handle()
                }
            }
    }

    fun getAllInventories(paginate: Boolean = false, search : Boolean = false){

        if (!paginate) {
            offset = 0
        } else if (paginate && !hasMoreData) {
            return
        }

        if (paginate) {
            paginateCount += 1
            Pair(HomeTrucksProgressItem() , DataRVAdapterOperationType.AddUpdate).let { userTrucksData.postValue( listOf(it)) }
        }

        val jsonObject = JsonObject()
        jsonObject.addProperty("supplier_id", userPrefs.userId())

        jsonObject.addProperty("offset", offset)
        jsonObject.addProperty("limit", UserTrucksLoadLimit)

        if(searchFlag){
            jsonObject.addProperty("vehicle_prefix",searchPrefix)
        }

        if(search && searchPrefix.isNotEmpty() ){
            mutableListOf<Pair<BaseHomeTrucksRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                add(Pair(HomeTrucksSearchItem(), DataRVAdapterOperationType.AddUpdate))
                add(Pair(HomeTrucksProgressItem() , DataRVAdapterOperationType.AddUpdate))
            }.let{userTrucksData.postValue(it)}

        }

        if(bodyTypeFilter.isNotEmpty()){
            val filter = mutableListOf<String>()
            for ( item in bodyTypeFilter){
                filter.add(item.second)
            }
             jsonObject.addProperty("truck_type", filter.joinToString(","))
        }
        if(availabilityFilter.isNotEmpty()) {
            val filter = mutableListOf<String>()
            for ( item in availabilityFilter){
                filter.add(item.second)
            }
            jsonObject.addProperty("availability", filter.joinToString(","))
        }
        if(sizeFilter.isNotNullOrEmpty()) {
            sizeFilter?.let { jsonObject.addProperty("truck_uuid", sizeFilter) }
        }

        dataLoadingLiveData.postValue(true)

        compositeDisposable += inventoryRepository.getInventories(jsonObject)
            .onBackground()
            .progress()
            .subscribe{ _res, error ->
                if(!error && _res != null) {
                    offset += _res.trucks.size
                    total = _res.total
                    hasMoreData = _res.hasNext

                    val trucksList :List<HomeTrucksRequestItemData> = _res.trucks

                    mutableListOf<Pair<BaseHomeTrucksRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        add(Pair(HomeTrucksProgressItem(), DataRVAdapterOperationType.Remove))

                        if(trucksList != null && trucksList.isNotEmpty()) {
                            add(Pair(HomeTrucksSearchItem(), DataRVAdapterOperationType.AddUpdate))
                            add(Pair(HomeTrucksFilterItem(), DataRVAdapterOperationType.AddUpdate))
                            if(!paginate) {
                                add(Pair( HomeTruckPriorityAccessItem(HomeTrucksPriorityItemData()), DataRVAdapterOperationType.AddUpdate))
                            }
                            add(Pair(HomeTrucksInfoItem(HomeTrucksInfoItemData(total)), DataRVAdapterOperationType.AddUpdate))

                            for (trucks in trucksList) {
                                add(Pair(HomeTrucksRequestItem(trucks), DataRVAdapterOperationType.AddUpdate))
                            }

                        }else{
                            bodyTypeFilter = mutableListOf()
                            availabilityFilter = mutableListOf()
                            sizeFilter = null
                            searchPrefix = ""
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
                            HomeTrucksWarningItem_TimeOut,
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

        if( currentCity.orion_db_city_code != null && destinationCity.orion_db_city_code != null) {
            compositeDisposable += inventoryRepository.getOriginDestinationCluster(
                currentCity.orion_db_city_code ?: "", destinationCity.orion_db_city_code ?: ""
            )
                .flatMap { t ->
                    val request = UpdateTruck(inventoryId, "activate_truck", currentCity.city, currentCity.orion_db_city_code!!,
                        destinationCity.city, destinationCity.orion_db_city_code!!, sourcedAs, t.first, t.second, price, "Free"
                    )
                    inventoryRepository.activateTruck(request.getRequest())
                }
                .onBackground()
                .progress()
                .subscribe { _res, error ->
                    if (!error && _res != null) {
                        activateTruckLiveData.postValue(Pair(position, _res))
                    } else {
                        if (error is HttpException) {
                            error.handle()
                            activateTruckLiveData.postValue(null)
                        } else {
                            activateTruckLiveData.postValue(Pair(-2, _res))
                        }
                    }
                }
        }
        else{
            noCityCodeError.postValue(true)
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

        if( currentCity.orion_db_city_code != null && destinationCity.orion_db_city_code != null) {
            compositeDisposable += inventoryRepository.getOriginDestinationCluster(
                currentCity.orion_db_city_code ?: "", destinationCity.orion_db_city_code ?: ""
            )
                .flatMap { t ->
                    val request = UpdateTruck(data.inventoryId, "update_details", currentCity.city, currentCity.orion_db_city_code!!, destinationCity.city,
                        destinationCity.orion_db_city_code!!, sourcedAs, t.first, t.second, price, ownership = ownership)

                    inventoryRepository.editTruck(request.getRequest())
                }
                .onBackground()
                .progress()
                .subscribe { _res, error ->
                    if (!error && _res != null) {
                        editTruckLiveData.postValue(Pair(position, _res))
                    } else {
                        if (error is HttpException) {
                            error.handle()
                            editTruckLiveData.postValue(null)
                        } else {
                            editTruckLiveData.postValue(Pair(-2, _res))
                        }

                    }
                }
        }
        else{
            noCityCodeError.postValue(true)
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