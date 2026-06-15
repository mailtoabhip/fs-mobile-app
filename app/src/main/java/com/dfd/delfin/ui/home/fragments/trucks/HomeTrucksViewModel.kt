package com.dfd.delfin.ui.home.fragments.trucks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.dfd.delfin.api.repository.LoadCycleRepository
import com.dfd.delfin.api.repository.LoadboardRepository
import com.dfd.delfin.api.repository.TruckRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.api.response.FastagBalanceResponse
import com.dfd.delfin.api.response.TruckResponseArray
import com.dfd.delfin.data.home.trucks.FastagStats
import com.dfd.delfin.data.home.trucks.HomeTrucksRequestItemData
import com.dfd.delfin.database.AppDatabase
import com.dfd.delfin.database.entity.OffersEntity
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.utils.extensions.isNotNullOrEmpty
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.gson.JsonObject
import io.reactivex.disposables.SerialDisposable
import javax.inject.Inject

/**
 * View model class for [HomeTrucksFragment]
 */

class HomeTrucksViewModel @Inject constructor(
    private val truckRepository: TruckRepository,
    private val userRepository: UserRepository,
    private val appDatabase: AppDatabase,
    private val loadCycleRepository: LoadCycleRepository,
    private val loadboardRepository: LoadboardRepository,
    val userPrefs: UserPrefs
): BaseViewModel() {

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

    private val inventoryDisposable = SerialDisposable()

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
        // Worker disabled — offers sync no longer needed
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

    override fun onCleared() {
        inventoryDisposable.dispose()
        super.onCleared()
    }

    /**
     * Submit FASTag lead request with simple parameters
     * Generic function that can be called from anywhere
     */
    fun submitFastagLead(
        vehicleCount: Int = 1,
        location: String? = null,
        vrn: String? = null,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = com.dfd.delfin.api.request.FastagLeadRequest(
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
