package com.delhivery.axle.ui.home.fragments.trucks

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.delhivery.axle.SyncOfferData.MyWorker
import com.delhivery.axle.api.repository.InventoryRepository
import com.delhivery.axle.api.repository.LoadCycleRepository
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.TruckRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.repository.UserTrucksLoadLimit
import com.delhivery.axle.api.request.DeactivateTruckRequest
import com.delhivery.axle.api.request.DeleteTruckRequest
import com.delhivery.axle.api.request.UpdateTruck
import com.delhivery.axle.api.response.FastagBalanceResponse
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.home.trucks.FastagStats
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.database.AppDatabase
import com.delhivery.axle.database.entity.OffersEntity
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.trucks.ActivateTruckInterface
import com.delhivery.axle.ui.trucks.EditTruckInterface
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonObject
import retrofit2.HttpException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * View model class for [HomeTrucksFragment]
 */

class HomeTrucksViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val truckRepository: TruckRepository,
    private val userRepository: UserRepository,
    private val appDatabase: AppDatabase,
    private val loadCycleRepository: LoadCycleRepository,
    private val loadboardRepository: LoadboardRepository,
    val userPrefs: UserPrefs
): BaseViewModel(), ActivateTruckInterface, EditTruckInterface {

    //private lateinit var application: Application
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

    /* FASTag stats live data */
    var fastagStatsData = MutableLiveData<FastagStats>()
    
    /* FASTag balance refresh live data - using tagId instead of position */
    var fastagBalanceRefreshData = MutableLiveData<Pair<String, FastagBalanceResponse>>()
    
    /* FASTag balance refresh error live data - using tagId instead of position */
    var fastagBalanceRefreshErrorData = MutableLiveData<Pair<String, String>>()
    
    /* FASTag balance refresh loading state - using tagId instead of position */
    var fastagBalanceRefreshLoadingData = MutableLiveData<Pair<String, Boolean>>()

    var noCityCodeError =  MutableLiveData<Boolean>()

    var mWorkManager: WorkManager? = null
    // The name of the Sync Data work
    val SYNC_DATA_WORK_NAME = "sync_data_work_name"
    private var mSavedWorkInfo: LiveData<List<WorkInfo>>? = null
    val TAG_SYNC_DATA = "TAG_SYNC_DATA"

    init {
        mWorkManager = WorkManager.getInstance()
        mSavedWorkInfo = mWorkManager?.getWorkInfosByTagLiveData(TAG_SYNC_DATA);
    }

    /* pagination params */
    var expectedArrivalTimePickup =MutableLiveData<Pair<String,Int>>()

    fun fetchTruckType() {
        compositeDisposable += truckRepository.getTruckType()
            .onBackground()
            .subscribe { _tRes, error ->
                if(!error && _tRes != null){
                    truckSizeData.clear()
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
        jsonObject.addProperty("supplier_id", userPrefs.parentId)

        jsonObject.addProperty("offset", offset)
        jsonObject.addProperty("limit", UserTrucksLoadLimit)

        if(searchFlag){
            jsonObject.addProperty("vehicle_prefix", searchPrefix.uppercase())
        }

        if(search && searchPrefix.isNotEmpty() ){
            mutableListOf<Pair<BaseHomeTrucksRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                // Search item removed - using fixed search bar in layout instead
                // add(Pair(HomeTrucksSearchItem(), DataRVAdapterOperationType.AddUpdate))
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

        compositeDisposable += loadboardRepository.getInventories(jsonObject)
            .onBackground()
            .subscribe{ _res, error ->
                if(!error && _res != null) {

                    // Update pagination state from API response
                    offset = _res.nextOffset ?: offset
                    
                    if (!searchFlag && !hasActiveFilters()) {
                        total = _res.total
                        userPrefs.inventoryCount = total.toString()
                    }
                    
                    hasMoreData = _res.hasNext

                    val trucksList :List<HomeTrucksRequestItemData> = _res.trucks
                    

                    if (!searchFlag && !hasActiveFilters()) {
                        // Calculate FASTag stats on background thread for large lists
                        compositeDisposable += io.reactivex.Single.fromCallable {
                            val fastagTrucksCount = trucksList.count { 
                                it.fastagTagStatus?.equals("Active", ignoreCase = true) == true 
                            }
                            val totalFastagBalance = trucksList
                                .filter { it.fastagTagStatus?.equals("Active", ignoreCase = true) == true }
                                .sumOf { it.fastagBalance?.toDoubleOrNull() ?: 0.0 }
                            
                            FastagStats(
                                totalTrucks = total,
                                fastagTrucksCount = fastagTrucksCount,
                                totalFastagBalance = totalFastagBalance
                            )
                        }
                        .onBackground()
                        .subscribe({ stats ->
                            fastagStatsData.postValue(stats)
                        }, { error ->
                            Log.e("HomeTrucksViewModel", "Error calculating FASTag stats", error)
                            // Post default stats on error
                            fastagStatsData.postValue(FastagStats(total, 0, 0.0))
                        })
                    }

                    mutableListOf<Pair<BaseHomeTrucksRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        add(Pair(HomeTrucksProgressItem(), DataRVAdapterOperationType.Remove))

                        if(trucksList != null && trucksList.isNotEmpty()) {
                            for (trucks in trucksList) {
                                add(Pair(HomeTrucksRequestItem(trucks), DataRVAdapterOperationType.AddUpdate))
                            }

                        }else{
                            Log.d("HomeTrucksViewModel", "Trucks list is empty or null")
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
        val request = DeactivateTruckRequest(data.inventoryId ?: "", "not_available","deactivate_truck", reason)
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

        if( currentCity.orionDbCityCode != null && destinationCity.orionDbCityCode != null) {
            compositeDisposable += inventoryRepository.getOriginDestinationCluster(
                currentCity.orionDbCityCode ?: "", destinationCity.orionDbCityCode ?: ""
            )
                .flatMap { t ->
                    val request = UpdateTruck(inventoryId, "activate_truck", currentCity.city, currentCity.orionDbCityCode!!,
                        destinationCity.city, destinationCity.orionDbCityCode!!, sourcedAs, t.first, t.second, userPrefs.demandType, price, "Free"
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

        if( currentCity.orionDbCityCode != null && destinationCity.orionDbCityCode != null) {
            compositeDisposable += inventoryRepository.getOriginDestinationCluster(
                currentCity.orionDbCityCode ?: "", destinationCity.orionDbCityCode ?: ""
            )
                .flatMap { t ->
                    val request = UpdateTruck(data.inventoryId ?: "", "update_details", currentCity.city, currentCity.orionDbCityCode!!, destinationCity.city,
                        destinationCity.orionDbCityCode!!, sourcedAs, t.first, t.second, userPrefs.demandType, price, ownership = ownership)

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
        compositeDisposable += inventoryRepository.deleteTruck(DeleteTruckRequest(data.inventoryId ?: ""))
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

    var  offersLiveData = ArrayList<OffersEntity>()

    var offeLiveData = MutableLiveData<HomeTrucksRequestItemData?>()

    fun fetchDatabaseOffers(data: HomeTrucksRequestItemData?){
//        val lrt = appDatabase.offersDao().getParticularsOffers(data?.currentCityCode, data?.unloadingDestinationCode)
//        if(!lrt.isNullOrEmpty() && lrt.size>0){
//            data?.resOffer = Triple(Pair(true, lrt.get(0).offerId),data?.truckSize, Pair(lrt[0].tdn,lrt[0].amt.toString()))
//        }else{
//            data?.resOffer = Triple(Pair(false, null),null, Pair(null,null))
//        }
//        offeLiveData.postValue(data)
    }

    fun fetchData() {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val periodicSyncDataWork = PeriodicWorkRequest.Builder(MyWorker::class.java, 24, TimeUnit.HOURS)
                .addTag(TAG_SYNC_DATA)
                .setConstraints(constraints) // setting a backoff on case the work needs to retry
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .build()
        mWorkManager?.enqueueUniquePeriodicWork(
                SYNC_DATA_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,  //Existing Periodic Work policy
                periodicSyncDataWork //work request
        )
    }

    fun refreshFastagBalance(tagId: String) {
        fastagBalanceRefreshLoadingData.postValue(Pair(tagId, true))
        
        compositeDisposable += loadboardRepository.getFastagBalance(tagId)
            .onBackground()
            .progress()
            .subscribe{ _res, error ->
                fastagBalanceRefreshLoadingData.postValue(Pair(tagId, false))
                
                if(!error && _res != null) {
                    fastagBalanceRefreshData.postValue(Pair(tagId, _res))
                } else {
                    error.handle()
                    val errorMessage = error.message ?: "Failed to refresh balance"
                    fastagBalanceRefreshErrorData.postValue(Pair(tagId, errorMessage))
                }
            }
    }
    
    /**
     * Check if any filters are currently active
     * @return true if any filter (vehicle type, availability, or truck size) is active
     */
    private fun hasActiveFilters(): Boolean {
        return bodyTypeFilter.isNotEmpty() || 
               availabilityFilter.isNotEmpty() || 
               sizeFilter.isNotNullOrEmpty()
    }

    /**
     * Submit FASTag lead request with simple parameters
     * Generic function that can be called from anywhere
     */
    fun submitFastagLead(
        vehicleCount: Int = 1,
        location: String = "",
        vrn: String? = null,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = com.delhivery.axle.api.request.FastagLeadRequest(
            userId = userPrefs.userId(),
            vehicleCount = vehicleCount,
            location = location,
            source = "Axle",
            vrn = vrn
        )
        
        compositeDisposable += loadboardRepository.submitFastagLead(request)
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                if (!error && _res != null) {
                    onSuccess(_res.message?:"")
                } else {
                    error.handle()
                    val errorMessage = error.message ?: "Failed to submit FASTag request"
                    onError(errorMessage)
                }
            }
    }

}
