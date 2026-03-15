package com.delhivery.axle.ui.home.fragments.loads

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.ContractType
import com.delhivery.axle.api.repository.DemandType
import com.delhivery.axle.api.repository.TransactionStatus.Requested
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.repository.TripsRepository
import com.delhivery.axle.api.repository.TruckRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.response.LowestBidResponse
import com.delhivery.axle.api.response.SearchAfter
import com.delhivery.axle.api.response.SpotMarketplaceLoadsData
import com.delhivery.axle.api.response.TransactionsResponse
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.Quintuple
import com.delhivery.axle.data.UserModel
import com.delhivery.axle.data.UserRespone
import com.delhivery.axle.data.bids.BulkBidCreateRequest
import com.delhivery.axle.data.bids.BulkBidRemoveRequest
import com.delhivery.axle.data.bids.BulkBidUpdateRequest
import com.delhivery.axle.data.bids.ModifyVehicleData
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.VehicleBidData
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.loads.HomeLoadsAddTruckItemDataConfig
import com.delhivery.axle.data.home.loads.HomeLoadsFilterItemData
import com.delhivery.axle.data.home.loads.HomeLoadsSearchItemData
import com.delhivery.axle.data.home.loads.HomeLoadsSummaryItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.biddetails.AcceptAdhocIntracityBidBottomDialogInterface
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialogInterface
import com.delhivery.axle.ui.biddetails.BulkBidsCreateEditInterface
import com.delhivery.axle.ui.dialogs.BidConfirmReviseDialogInterface
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject
import io.reactivex.functions.Function3
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.Resource
import kotlinx.coroutines.CancellationException

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * View model class for [HomeLoadsFragment]
 *
 **
 */
class HomeLoadsViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val userRepository: UserRepository,
  private val bidsRepository: BidsRepository,
  private val truckRepository: TruckRepository,
  private val tripsRepository: TripsRepository,
  val userPrefs: UserPrefs
) : BaseViewModel(), BidDetailsCreateEditDialogInterface, BidConfirmReviseDialogInterface, BulkBidsCreateEditInterface, AcceptAdhocIntracityBidBottomDialogInterface {

  /* user bids live data */
  var userLoadsData =
    MutableLiveData<List<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>>()
  var userLoadsDataFetch =
    MutableLiveData<List<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* route/lane preferene live data */
  var routesLiveData = MutableLiveData<Boolean>()

  /* bid action result live data */
  var bidsActionLiveData = MutableLiveData<Pair<Int, TransactionBid>>()

  var bulkBidActionLiveData = MutableLiveData<Pair<Int,List<TransactionBid>>>()
  var acceptBidLiveData = MutableLiveData<Pair<Int,Any>>()


    var lowestBidLiveData = MutableLiveData<Pair<Int, HomeBidsRequestItemData>>()

  /* revise bid live data */
  var reviseBidLiveData = MutableLiveData<Pair<Boolean, Int>>()

  /* loads count live data */
  var loadsCountLiveData = MutableLiveData<Int>()

    var fullLoadsCountLiveData = MutableLiveData<Int>()

    /* data loading live data */
  var dataLoadingLiveData = MutableLiveData<Boolean>()

  var truckGetLiveData = MutableLiveData<Pair<List<TruckResponseArray>,HomeBidsRequestItemData>>()

  var loadPricePercent = 0

  /* pagination params */
  var hasMoreData = true
  var fecthToCalled = true
  var more_default_loads = false
  var vehicleTypes: String?= null
  var passing_vehicle_type: String?= null
  var filterVehicleType: Boolean?= null
  var offset = 0
  var offsetFetch = 0
  var total = 0
  var totalFetch = 0
  var totalFetchTitle = 0
  var transactionIds:String?=null

  var hasOrionLoadOnce = false

  /* vehicle_type filter */
  var vehicleStr = userPrefs.truckTypes
  //var type = userPrefs.demandType

  var paginateCount =0

    var txnIds:ArrayList<String> = ArrayList()


  var editBulkLiveData= MutableLiveData<Pair<Int,String>>()
  var editFlg= mutableListOf<Boolean>(false,false,false)

  var loadsCount:Int =0
    var searchAfter: SearchAfter?=null
    var cachedMarketplaceCount:Int = 0

    var user: UserModel? = null

    val hasMarketplaceAccess = userPrefs.demandType.contains(DemandType.Spot_Marketplace.type)

    var intercityListShownTracked = MutableLiveData<Boolean>().apply { value = false }
    var intracityListShownTracked = MutableLiveData<Boolean>().apply { value = false }
    var marketPlaceListShownTracked = MutableLiveData<Boolean>().apply { value = false }

    /* coroutine job for cancellation on rapid tab switching */
    private var currentFetchJob: Job? = null

    /**
   * Getter/Setter for route update flag to preferences
   */
  var routeUpdated: Boolean
    get() = userPrefs.routeUpdate
    set(value) {
      userPrefs.routeUpdate = value
    }

  /**
   * Check FCM registration flag
   */
  fun isFCMTokenGenerated() = userPrefs.fcmTokenGenerated

  fun isMoengageFCMTokenGenerated() = userPrefs.moengageFcmTokenGenerated


  var fromNotification: Boolean
    get() = userPrefs.fromNotification
    set(value) {
      userPrefs.fromNotification = value
    }

    /**
     * Helper method to populate payment fields from user data
     */
    private fun populatePaymentFields(load: HomeBidsRequestItemData) {
        user?.supplierDetails?.let { supplier ->
            load.paymentMode = supplier.paymentMode
            load.advancePercentage = supplier.advancePercentage
        }
    }

    /**
     * Fetch user [Requested] transactions
     */
    fun fetchUserTransactions(
            paginate: Boolean = false, demandType: String,
            selectedFilter: String = "", infoSearch: Boolean = false, excludeTruckTypes: String?= null) {
        if (!paginate || infoSearch) {
            searchAfter = null
            offset = 0
            offsetFetch = 0
            totalFetchTitle =0
            hasMoreData = true
        } else if (paginate && !hasMoreData) {
            return
        }

        if (!paginate) {
            txnIds.clear()
        }

        if (paginate) {
            paginateCount += 1
            Pair(HomeLoadsProgressItem(), AddUpdate).let { userLoadsData.postValue(listOf(it)) }
        }

        passing_vehicle_type = vehicleStr
        vehicleTypes = passing_vehicle_type
        if (infoSearch) {
            vehicleTypes = null
        }

        dataLoadingLiveData.postValue(true)
        
        // Fetch user data first if not available
        if (user == null) {
            // Cancel previous fetch before starting user fetch
            currentFetchJob?.cancel()
            
            compositeDisposable += userRepository.getUser(false)
                .onBackground()
                .subscribe { userModel, error ->
                    if (!error && userModel != null) {
                        this@HomeLoadsViewModel.user = userModel
                        // Now proceed with fetching loads using coroutines
                        currentFetchJob = viewModelScope.launch {
                            try {
                                fetchLoadsData(paginate, demandType, selectedFilter, infoSearch, excludeTruckTypes)
                            } catch (e: Exception) {
                                if (e !is CancellationException) {
                                    Log.e("HomeLoadsViewModel", "Error fetching loads", e)
                                }
                            } finally {
                                dataLoadingLiveData.postValue(false)
                            }
                        }
                    } else {
                        error?.handle()
                        dataLoadingLiveData.postValue(false)
                    }
                }
        } else {
            // Cancel previous fetch for rapid tab switching
            currentFetchJob?.cancel()
            
            // User data already available, proceed with loads
            currentFetchJob = viewModelScope.launch {
                try {
                    fetchLoadsData(paginate, demandType, selectedFilter, infoSearch, excludeTruckTypes)
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        Log.e("HomeLoadsViewModel", "Error in fetchUserTransactions", e)
                    }
                } finally {
                    dataLoadingLiveData.postValue(false)
                }
            }
        }
    }
    
    /**
     * Fetch loads data (separated from user data fetching) - Coroutine version
     */
    private suspend fun fetchLoadsData(
        paginate: Boolean, demandType: String, selectedFilter: String, 
        infoSearch: Boolean, excludeTruckTypes: String?) {
        val mainTrace = Firebase.performance.newTrace("fetch_recommended_transactions")
        val parallelTrace = Firebase.performance.newTrace("fetch_bids_for_recommended_transactions_parallel")
        mainTrace.start()
        
        if(selectedFilter == DemandType.Intracity.type) {
            // --- Intracity branch ---
            val primaryResult = transactionsRepository.fetchIntracityRecommTransactions(
                offset, demandType, vehicleTypes, excludeTruckTypes,
                filterVehicleType, true, null, searchAfter
            )
            
            when (primaryResult) {
                is Resource.Success -> {
                    val _res = primaryResult.data!!
                    
                    // Update pagination state
                    offset = _res.offset ?: 0
                    total = _res.transactions?.size ?: 0
                    searchAfter = _res.searchAfter
                    if (total == 0) {
                        searchAfter = null
                        hasMoreData = false
                    }
                    fecthToCalled = _res.offset < _res.total
                    loadPricePercent = _res.loadPricePercent
                    more_default_loads = _res.more_loads
                    loadsCountLiveData.postValue(total)
                    
                    // 2 parallel calls for split counts + marketplace
                    val parallelResults = coroutineScope {
                        val splitCountDeferred = async {
                            transactionsRepository.fetchRecommTransactions(
                                offset,
                                filterDemandTypeForRecommendations(userPrefs.demandType),
                                vehicleTypes, excludeTruckTypes, filterVehicleType,
                                true, splitViewCount = true, searchAfter = null
                            )
                        }
                        val marketplaceDeferred = async {
                            if (hasMarketplaceAccess) {
                                transactionsRepository.fetchSpotMarketplaceTransactions(
                                    onlyCount = true, limit = 100, offset = 0
                                )
                            } else {
                                Resource.Success(SpotMarketplaceLoadsData(
                                    totalCount = 0, limit = 0, offset = 0,
                                    hasNext = false, transactions = emptyList()
                                ))
                            }
                        }
                        Pair(splitCountDeferred.await(), marketplaceDeferred.await())
                    }
                    
                    // Extract tab counts
                    var intercityCount = 0
                    var nonDlvCount = 0
                    var marketplaceCount = 0
                    
                    when (val splitResult = parallelResults.first) {
                        is Resource.Success -> {
                            splitResult.data?.loadCounts?.all?.forEach { item ->
                                when (item.key) {
                                    "INTERCITY" -> intercityCount = item.count ?: 0
                                    "NON_DELHIVERY" -> nonDlvCount = item.count ?: 0
                                }
                            }
                        }
                        else -> { /* counts remain 0 */ }
                    }
                    
                    when (val marketplaceResult = parallelResults.second) {
                        is Resource.Success -> {
                            marketplaceCount = marketplaceResult.data?.totalCount ?: 0
                            cachedMarketplaceCount = marketplaceCount
                        }
                        else -> { /* count remains 0 */ }
                    }
                    
                    // Build UI items list
                    mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        add(Pair(HomeLoadsProgressItem(), Remove))
                        
                        val loads = _res.transactions ?: emptyList()
                        
                        add(Pair(HomeLoadsSearchItem(HomeLoadsSearchItemData(vehicleTypes)), AddUpdate))
                        
                        val count = total + intercityCount + nonDlvCount + marketplaceCount
                        Log.d("viewmodel", "$total intrcity $intercityCount nonDlv $nonDlvCount marketplace $marketplaceCount")
                        Log.d("count", count.toString())
                        fullLoadsCountLiveData.postValue(count)
                        
                        add(Pair(
                            HomeLoadsFilterItem(HomeLoadsFilterItemData(
                                selectedFilter, total, intercityCount, nonDlvCount,
                                marketplaceCount, userPrefs.demandType
                            )), AddUpdate
                        ))
                        
                        if (!paginate && userPrefs.verificationStatus.equals("failed")) {
                            add(Pair(HomeLoadsKycPendingItem(), AddUpdate))
                        }
                        
                        if (!paginate) {
                            add(Pair(HomeLoadsTruckPriorityAccessItem(), AddUpdate))
                        }
                        
                        if (total == 0 && !paginate) {
                            add(Pair(HomeLoadsWarningItem_NoLoads, AddUpdate))
                        }
                        
                        for ((index, load) in loads.toMutableList().withIndex()) {
                            try {
                                load.transactionId?.let { txnIds.add(it) }
                            } catch (e: Exception) {
                                Log.d("No Bid found for: ", load.transactionId ?: "")
                            }
                            populatePaymentFields(load)
                            add(Pair(HomeLoadsRequestItem(load), Add))
                        }
                        
                        if (!hasMoreData && !hasOrionLoadOnce && more_default_loads) {
                            add(Pair(HomeLoadsInfoItem(), Remove))
                            add(Pair(HomeLoadsInfoItem(), AddUpdate))
                        }
                        add(Pair(HomeLoadsMoreInfoItem(), Remove))
                        add(Pair(HomeLoadsMoreInfoItem(), AddUpdate))
                    }.let {
                        userLoadsData.postValue(it)
                        if (_res.transactions?.isNotEmpty() == true && paginateCount == 0)
                            intracityListShownTracked.postValue(true)
                    }
                }
                
                is Resource.Failure -> {
                    // Post error state to UI (NO fallback for intracity)
                    Log.e("HomeLoadsViewModel", "Error fetching intracity loads: ${primaryResult.apiError}")
                    
                    // Post empty list with error item to UI
                    mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        add(Pair(HomeLoadsProgressItem(), Remove))
                        add(Pair(HomeLoadsSearchItem(HomeLoadsSearchItemData(vehicleTypes)), AddUpdate))
                        add(Pair(HomeLoadsFilterItem(HomeLoadsFilterItemData(
                            selectedFilter, 0, 0, 0, 0, userPrefs.demandType
                        )), AddUpdate))
                        add(Pair(HomeLoadsWarningItem_TimeOut, AddUpdate))
                        add(Pair(HomeLoadsMoreInfoItem(), AddUpdate))
                    }.let {
                        userLoadsData.postValue(it)
                    }
                }
                
                is Resource.Loading -> { /* not expected */ }
            }
            mainTrace.stop()
        } else {
            // --- Non-intracity branch ---
            Log.d("Viewmod1", "in else")
            
            val primaryResult = transactionsRepository.fetchRecommTransactions(
                offset,
                filterDemandTypeForRecommendations(userPrefs.demandType),
                vehicleTypes, excludeTruckTypes, filterVehicleType,
                true, searchAfter = searchAfter
            )
            
            when (primaryResult) {
                is Resource.Success -> {
                    val _res = primaryResult.data!!
                    
                    Log.d("Viewmode123", "${_res.toString()}")
                    Log.d("Viewmode12", "${_res.transactions}")
                    
                    // Update pagination state
                    searchAfter = _res.searchAfter
                    hasMoreData = searchAfter != null
                    offset = _res.offset
                    total = _res.transactions?.size ?: 0
                    if (total == 0) {
                        searchAfter = null
                        hasMoreData = false
                    }
                    fecthToCalled = _res.offset < _res.total
                    loadPricePercent = _res.loadPricePercent
                    more_default_loads = _res.more_loads
                    loadsCount += total
                    
                    parallelTrace.start()
                    
                    // 5 parallel calls
                    data class ParallelResults(
                        val bids: Resource<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>>,
                        val lowestBids: Resource<Pair<List<HomeBidsRequestItemData>, List<com.delhivery.axle.api.response.LowestBidResponse>>>,
                        val intracity: Resource<TransactionsResponse>,
                        val splitCount: Resource<TransactionsResponse>,
                        val marketplace: Resource<SpotMarketplaceLoadsData>
                    )
                    
                    val parallelResults = coroutineScope {
                        val bidsDeferred = async {
                            bidsRepository.bidsForLoads(_res.transactions)
                        }
                        val lowestBidsDeferred = async {
                            bidsRepository.bulkLowestBidsForLoads(_res.transactions)
                        }
                        val intracityDeferred = async {
                            transactionsRepository.fetchIntracityRecommTransactions(
                                offset, userPrefs.demandType, vehicleTypes,
                                excludeTruckTypes, filterVehicleType, true
                            )
                        }
                        val splitCountDeferred = async {
                            transactionsRepository.fetchRecommTransactions(
                                offset,
                                filterDemandTypeForRecommendations(userPrefs.demandType),
                                vehicleTypes, excludeTruckTypes, filterVehicleType,
                                true, splitViewCount = true, searchAfter = null
                            )
                        }
                        val marketplaceDeferred = async {
                            if (hasMarketplaceAccess) {
                                transactionsRepository.fetchSpotMarketplaceTransactions(
                                    onlyCount = true, limit = 100, offset = 0
                                )
                            } else {
                                Resource.Success(SpotMarketplaceLoadsData(
                                    totalCount = 0, limit = 0, offset = 0,
                                    hasNext = false, transactions = emptyList()
                                ))
                            }
                        }
                        
                        ParallelResults(
                            bidsDeferred.await(),
                            lowestBidsDeferred.await(),
                            intracityDeferred.await(),
                            splitCountDeferred.await(),
                            marketplaceDeferred.await()
                        )
                    }
                    
                    parallelTrace.stop()
                    mainTrace.stop()
                    
                    // Extract data from parallel results
                    val loads = _res.transactions ?: emptyList()
                    val bids = when (parallelResults.bids) {
                        is Resource.Success -> parallelResults.bids.data?.second ?: emptyList()
                        else -> emptyList()
                    }
                    
                    // Extract tab counts
                    var intercityCount = 0
                    var nonDlvCount = 0
                    var marketplaceCount = 0
                    
                    when (val splitResult = parallelResults.splitCount) {
                        is Resource.Success -> {
                            splitResult.data?.loadCounts?.all?.forEach { item ->
                                when (item.key) {
                                    "INTERCITY" -> intercityCount = item.count ?: 0
                                    "NON_DELHIVERY" -> nonDlvCount = item.count ?: 0
                                }
                            }
                        }
                        else -> { /* counts remain 0 */ }
                    }
                    
                    when (val marketplaceResult = parallelResults.marketplace) {
                        is Resource.Success -> {
                            marketplaceCount = marketplaceResult.data?.totalCount ?: 0
                            cachedMarketplaceCount = marketplaceCount
                        }
                        else -> { /* count remains 0 */ }
                    }
                    
                    val count = if (selectedFilter == DemandType.Internal.type) {
                        intercityCount
                    } else {
                        nonDlvCount
                    }
                    loadsCountLiveData.postValue(count)
                    
                    // Build UI items list
                    mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        add(Pair(HomeLoadsProgressItem(), Remove))
                        
                        add(Pair(HomeLoadsSearchItem(HomeLoadsSearchItemData(vehicleTypes)), AddUpdate))
                        
                        val totalCount = when (val splitResult = parallelResults.splitCount) {
                            is Resource.Success -> splitResult.data?.total ?: 0
                            else -> 0
                        }
                        val fullCount = totalCount + intercityCount + nonDlvCount + marketplaceCount
                        Log.d("LoadView", "$totalCount inter $intercityCount nonDlv $nonDlvCount marketplace $marketplaceCount")
                        fullLoadsCountLiveData.postValue(fullCount)
                        
                        add(Pair(
                            HomeLoadsFilterItem(HomeLoadsFilterItemData(
                                selectedFilter, totalCount, intercityCount, nonDlvCount,
                                marketplaceCount, userPrefs.demandType
                            )), AddUpdate
                        ))
                        
                        if (!paginate && userPrefs.verificationStatus.equals("failed")) {
                            add(Pair(HomeLoadsKycPendingItem(), AddUpdate))
                        }
                        
                        if (!paginate) {
                            add(Pair(HomeLoadsTruckPriorityAccessItem(), AddUpdate))
                        }
                        
                        if (total == 0 && !paginate) {
                            add(Pair(HomeLoadsWarningItem_NoLoads, AddUpdate))
                        }
                        
                        for ((index, load) in loads.toMutableList().withIndex()) {
                            try {
                                load.transactionId?.let { txnIds.add(it) }
                            } catch (e: Exception) {
                                Log.d("No Bid found for: ", load.transactionId ?: "")
                            }
                            populatePaymentFields(load)
                            
                            if (index.rem(HomeLoadsAddTruckItemDataConfig) == 0 && index != 0) {
                                add(Pair(HomeLoadsAddTruckItem(), Add))
                            }
                            add(Pair(HomeLoadsRequestItem(load), Add))
                        }
                        
                        if (!hasMoreData && !hasOrionLoadOnce && more_default_loads && totalFetchTitle < total) {
                            add(Pair(HomeLoadsInfoItem(), AddUpdate))
                        }
                        add(Pair(HomeLoadsMoreInfoItem(), AddUpdate))
                    }.let {
                        userLoadsData.postValue(it)
                        if (loads.isNotEmpty() && paginateCount == 0) {
                            intercityListShownTracked.postValue(true)
                        }
                    }
                }
                
                is Resource.Failure -> {
                    if (primaryResult.apiError != null) {
                        mainTrace.putAttribute("error_response_received", primaryResult.apiError.toString())
                    }
                    parallelTrace.stop()
                    mainTrace.stop()
                    
                    // CRITICAL: Fallback to supplier loads on error
                    fetchSupplierTransactions(
                        totalFetchTitle > 0,
                        selectedFilter,
                        demandType,
                        infoSearch,
                        excludeTruckTypes
                    )
                }
                
                is Resource.Loading -> { /* not expected */ }
            }
        }
    }

    /**
   * Fetch user [Requested] transactions
   */
  fun fetchSupplierTransactions(
    paginate: Boolean = false, selectedFilter: String,demandType: String,
    infoSearch: Boolean = false, excludeTruckTypes: String?= null) {
    if (!paginate || infoSearch) {
      offsetFetch = 0
      searchAfter = null
    } else if (paginate && !hasMoreData ) {
      return
    }

    if (paginate) {
      paginateCount += 1
      Pair(HomeLoadsProgressItem(), AddUpdate).let { userLoadsDataFetch.postValue(listOf(it)) }
    }
      val distinct = txnIds.distinct().toList()
      for(txn in distinct){
        if(transactionIds.isNotNullOrEmpty()){
          transactionIds=transactionIds+","+txn
        }else{
          transactionIds=txn
        }
      }

    passing_vehicle_type = vehicleStr
    vehicleTypes = passing_vehicle_type
    if (infoSearch) {
      vehicleTypes = null
    }

    dataLoadingLiveData.postValue(true)
    
    // Fetch user data first if not available
    if (user == null) {
        compositeDisposable += userRepository.getUser(false)
            .onBackground()
            .subscribe { userModel, error ->
                if (!error && userModel != null) {
                    this.user = userModel
                    // Now proceed with fetching supplier loads
                    fetchSupplierLoadsData(paginate, selectedFilter, demandType, infoSearch, excludeTruckTypes)
                } else {
                    error?.handle()
                    dataLoadingLiveData.postValue(false)
                }
            }
    } else {
        // User data already available, proceed with supplier loads
        fetchSupplierLoadsData(paginate, selectedFilter, demandType, infoSearch, excludeTruckTypes)
    }
  }
  
  /**
   * Fetch supplier loads data (separated from user data fetching)
   * Uses RxJava wrappers for repository methods
   */
  private fun fetchSupplierLoadsData(
    paginate: Boolean, selectedFilter: String, demandType: String,
    infoSearch: Boolean, excludeTruckTypes: String?) {

      compositeDisposable += transactionsRepository.fetchLoadBoardTransactions(offsetFetch, demandType, vehicleTypes, excludeTruckTypes, filterVehicleType, true, transactionIds)
                              .flatMap { t ->
                                  offsetFetch = t.offset
                                  totalFetchTitle = total+t.total
                                  totalFetch=t.total
                                  hasMoreData =t.hasNext
                                  loadPricePercent = t.loadPricePercent
                                  more_default_loads = t.more_loads
                                  loadsCountLiveData.postValue(totalFetchTitle)

                                  Single.zip(
                                      bidsRepository.bidsForLoadsRx(t.transactions).subscribeOn(Schedulers.io()),
                                      bidsRepository.bulkLowestBidsForLoadsRx(t.transactions).subscribeOn(Schedulers.io()),
                                      transactionsRepository.fetchIntracityRecommTransactionsRx(offset,
                                          demandType,
                                          vehicleTypes,
                                          excludeTruckTypes,
                                          filterVehicleType,
                                          true, onlyCount = true).subscribeOn(Schedulers.io()),
                                      Function3<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>, Pair<List<HomeBidsRequestItemData>, List<LowestBidResponse>>, TransactionsResponse,
                                              Quintuple<List<HomeBidsRequestItemData>, List<TransactionBid>, List<LowestBidResponse>,TransactionsResponse,TransactionsResponse>> { t1, t2,t3 ->
                                          Quintuple(t1.first, t1.second, t2.second,t3,t)
                                          })
                              }
        .onBackground()
        .subscribe { _tRes, error ->
          if (!error && _tRes != null) {
            mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeLoadsProgressItem(), Remove))

                val loadsWithBids = _tRes.first ?: emptyList()
                val bids = _tRes.second
                val bidTransactionIds = bids.map { it.transactionId }.toSet()

                val loads = loadsWithBids.filter { load ->
                    load.transactionId !in bidTransactionIds
                }

                    var intercityCount =0
                    var nonDlvCount = 0
                    var marketplaceCount = 0
                    if(demandType==DemandType.Internal.type){
                        intercityCount = _tRes.fifth.total
                    }else{
                        nonDlvCount = _tRes.fifth.total
                    }
                    
                    // Fetch marketplace count inline
                    compositeDisposable += transactionsRepository.fetchSpotMarketplaceTransactionsRx(
                        onlyCount = true,
                        limit = 100,
                        offset = 0
                    ).onBackground()
                    .subscribe { marketplaceRes, marketplaceError ->
                        if (!marketplaceError) {
                            marketplaceCount = marketplaceRes?.let { data -> data.totalCount } ?: 0
                            cachedMarketplaceCount = marketplaceCount
                            Log.d("MarketplaceCount", "Fetched: $marketplaceCount")
                        }
                    }
                    
                  add(Pair(HomeLoadsSearchItem(HomeLoadsSearchItemData(vehicleTypes)), AddUpdate))
                val count = _tRes.fourth.total+intercityCount+nonDlvCount+marketplaceCount
                fullLoadsCountLiveData.postValue(count)
                  add(Pair(HomeLoadsFilterItem(HomeLoadsFilterItemData(selectedFilter,_tRes.fourth.total,intercityCount,nonDlvCount,marketplaceCount,userPrefs.demandType)), AddUpdate))
                  if(!paginate) {
//                    add(Pair(HomeLoadsShareRateItem(HomeLoadsShareRateItemData(true,userPrefs.shareRateBannerH1,userPrefs.shareRateBannerH3,userPrefs.shareRateBannerH2)), AddUpdate))
                    add(Pair(HomeLoadsTruckPriorityAccessItem(), AddUpdate))
                  }
          //      add(Pair(HomeLoadsSummaryItem(HomeLoadsSummaryItemData(totalFetchTitle)), AddUpdate))
                if (total == 0  && !paginate) {
                    add(Pair(HomeLoadsWarningItem_NoLoads, AddUpdate))
                }
                  for ((index, load) in loads.toMutableList().withIndex()) {
                  /*    try {
                          val lowestBid = _tRes.third.filter { b ->
                              b.transactionId.safeEquals(load.transactionId)
                          }[0]
                          load.lowestBid = lowestBid.minBid
                          load.numBids = lowestBid.numBids
                          load.loadPricePercent = loadPricePercent
                          load.transactionBid =
                              bids.filter { b ->
                                  b.transactionId.safeEquals(load.transactionId)
                              }[0]
                          if (load.isDMTIndent()) {
                              load.bulkTransactionBids =
                                  bids.filter { b ->
                                      b.transactionId.safeEquals(load.transactionId)
                                  }
                          }
                      } catch (e: Exception) {
                          Log.d("No Bid found for: ", load.transactionId ?: "")
                      }*/
                      // Populate payment fields from user data
                      populatePaymentFields(load)
                      if (index.rem(HomeLoadsAddTruckItemDataConfig) == 0 && index != 0) {
                          add(Pair(HomeLoadsAddTruckItem(), Add))
                      }
                      add(Pair(HomeLoadsRequestItem(load), Add))
                  }

                  if (!hasMoreData && !hasOrionLoadOnce) {
                    if (more_default_loads){
                      add(Pair(HomeLoadsInfoItem(), Remove))
                      add(Pair(HomeLoadsInfoItem(), AddUpdate))
                    }else{
                      add(Pair(HomeLoadsInfoItem(), Remove))
                    }
                      add(Pair(HomeLoadsMoreInfoItem(), Remove))
                      add(Pair(HomeLoadsMoreInfoItem(), AddUpdate))
                  }



            }
                .let {
                  userLoadsDataFetch.postValue(it) }

          } else {
            mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeLoadsProgressItem(), Remove))
              /* add api time out item */
              if(total==0)
                add(Pair(HomeLoadsWarningItem_TimeOut, AddUpdate))
              add(Pair(HomeLoadsMoreInfoItem(),Remove))
              add(Pair(HomeLoadsMoreInfoItem(), AddUpdate))
              hasMoreData=false
            }
                .let {
                  if(!userLoadsData.value?.isNullOrEmpty()&& totalFetch>0){
                    if(userLoadsData.value!=null && userLoadsData.value!!.size>0){
                    it.addAll(userLoadsData.value!!)}
                  }

                  userLoadsDataFetch.postValue(it) }
          }

          dataLoadingLiveData.postValue(false)
        }
  }

  /**
   * Fetch spot marketplace transactions
   */
  fun fetchSpotMarketplaceLoads(
    paginate: Boolean = false,
    onlyCount: Boolean = false,
    limit: Int = 100
  ) {
    if (!paginate) {
      offset = 0
      searchAfter = null
      hasMoreData = true
    } else if (paginate && !hasMoreData) {
      return
    }

    if (paginate) {
      paginateCount += 1
      Pair(HomeLoadsProgressItem(), AddUpdate).let { userLoadsData.postValue(listOf(it)) }
    }

    dataLoadingLiveData.postValue(true)
    
    // Fetch user data first if not available
    if (user == null) {
      // Cancel previous fetch before starting user fetch
      currentFetchJob?.cancel()
      
      compositeDisposable += userRepository.getUser(false)
        .onBackground()
        .subscribe { userModel, error ->
          if (!error && userModel != null) {
            this@HomeLoadsViewModel.user = userModel
            // Now proceed with fetching marketplace loads using coroutines
            currentFetchJob = viewModelScope.launch {
              try {
                fetchMarketplaceLoadsData(paginate, onlyCount, limit)
              } catch (e: Exception) {
                if (e !is CancellationException) {
                  Log.e("HomeLoadsViewModel", "Error fetching marketplace loads", e)
                }
              } finally {
                dataLoadingLiveData.postValue(false)
              }
            }
          } else {
            error?.handle()
            dataLoadingLiveData.postValue(false)
          }
        }
    } else {
      // Cancel previous fetch for rapid tab switching
      currentFetchJob?.cancel()
      
      // User data already available, proceed with marketplace loads
      currentFetchJob = viewModelScope.launch {
        try {
          fetchMarketplaceLoadsData(paginate, onlyCount, limit)
        } catch (e: Exception) {
          if (e !is CancellationException) {
            Log.e("HomeLoadsViewModel", "Error in fetchSpotMarketplaceLoads", e)
          }
        } finally {
          dataLoadingLiveData.postValue(false)
        }
      }
    }
  }

  /**
   * Fetch marketplace loads data (separated from user data fetching) - Coroutine version
   */
  private suspend fun fetchMarketplaceLoadsData(
    paginate: Boolean,
    onlyCount: Boolean,
    limit: Int
  ) {
    val mainTrace = Firebase.performance.newTrace("fetch_spot_marketplace_transactions")
    mainTrace.start()

    // Step 1: Fetch count only
    val countResult = transactionsRepository.fetchSpotMarketplaceTransactions(
      onlyCount = true, limit = limit, offset = 0
    )
    
    when (countResult) {
      is Resource.Success -> {
        val count = countResult.data?.totalCount ?: 0
        Log.d("MarketplaceDebug", "Count API Response - count: $count")
        total = count
        cachedMarketplaceCount = total
        loadsCountLiveData.postValue(count)
      }
      is Resource.Failure -> {
        Log.e("HomeLoadsViewModel", "Error fetching marketplace count: ${countResult.apiError}")
        mainTrace.stop()
        return
      }
      else -> { /* continue */ }
    }
    
    // Step 2: Fetch actual data
    val dataResult = transactionsRepository.fetchSpotMarketplaceTransactions(
      onlyCount = false, limit = limit, offset = offset
    )
    
    when (dataResult) {
      is Resource.Success -> {
        val _res = dataResult.data!!
        val transactions = _res.transactions
        
        Log.d("MarketplaceDebug", "Data API Response - transactions: ${transactions?.size ?: 0}")
        
        // Update pagination state
        offset = _res.offset ?: 0
        if (total == 0) {
          hasMoreData = false
        }
        hasMoreData = _res.hasNext ?: false
        Log.d("MarketplaceDebug", "About to fetch bids for ${transactions?.size ?: 0} transactions")
        
        // Step 3: 2 parallel calls for bids
        data class BidsResults(
          val bids: Resource<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>>,
          val lowestBids: Resource<Pair<List<HomeBidsRequestItemData>, List<LowestBidResponse>>>
        )
        
        val bidsResults = coroutineScope {
          val bidsDeferred = async {
            bidsRepository.bidsForLoads(transactions)
          }
          val lowestBidsDeferred = async {
            bidsRepository.bulkLowestBidsForLoads(transactions)
          }
          
          BidsResults(
            bidsDeferred.await(),
            lowestBidsDeferred.await()
          )
        }
        
        mainTrace.stop()
        
        // Extract bids data
        val loads = transactions ?: emptyList()
        val bids = when (bidsResults.bids) {
          is Resource.Success -> bidsResults.bids.data?.second ?: emptyList()
          else -> emptyList()
        }
        val lowestBids = when (bidsResults.lowestBids) {
          is Resource.Success -> bidsResults.lowestBids.data?.second ?: emptyList()
          else -> emptyList()
        }
        
        Log.d("MarketplaceDebug", "Processing marketplace data - loads count: ${loads.size}, bids: ${bids.size}, lowestBids: ${lowestBids.size}")
        
        // Build UI items
        mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
          add(Pair(HomeLoadsProgressItem(), Remove))
          
          add(Pair(HomeLoadsSearchItem(HomeLoadsSearchItemData(vehicleTypes)), AddUpdate))
          
          // Cache the marketplace count (already set from the API response)
          cachedMarketplaceCount = total
          
          // Fetch cross-tab counts (nested parallel call)
          viewModelScope.launch {
            data class CrossTabCounts(
              val intracity: Resource<TransactionsResponse>,
              val intercity: Resource<TransactionsResponse>
            )
            
            val crossTabResults = coroutineScope {
              val intracityDeferred = async {
                transactionsRepository.fetchIntracityRecommTransactions(
                  offset, userPrefs.demandType, vehicleTypes, null,
                  filterVehicleType, true, onlyCount = true
                )
              }
              val intercityDeferred = async {
                transactionsRepository.fetchRecommTransactions(
                  offset,
                  filterDemandTypeForRecommendations(userPrefs.demandType),
                  vehicleTypes, null, filterVehicleType, true,
                  splitViewCount = true, searchAfter = null
                )
              }
              
              CrossTabCounts(
                intracityDeferred.await(),
                intercityDeferred.await()
              )
            }
            
            // Extract counts
            val intracityCount = when (crossTabResults.intracity) {
              is Resource.Success -> crossTabResults.intracity.data?.total ?: 0
              else -> 0
            }
            
            var intercityCount = 0
            var nonDlvCount = 0
            when (val intercityResult = crossTabResults.intercity) {
              is Resource.Success -> {
                intercityResult.data?.loadCounts?.all?.forEach { item ->
                  when (item.key) {
                    "INTERCITY" -> intercityCount = item.count ?: 0
                    "NON_DELHIVERY" -> nonDlvCount = item.count ?: 0
                  }
                }
              }
              else -> { /* counts remain 0 */ }
            }
            
            val totalAllCounts = intracityCount + intercityCount + nonDlvCount + cachedMarketplaceCount
            fullLoadsCountLiveData.postValue(totalAllCounts)
            
            // Update filter with all counts
            val filterItem = HomeLoadsFilterItem(
              HomeLoadsFilterItemData(
                "Marketplace", intracityCount, intercityCount,
                nonDlvCount, cachedMarketplaceCount, userPrefs.demandType
              )
            )
            userLoadsData.postValue(listOf(Pair(filterItem, AddUpdate)))
          }
          
          // Add filter item with marketplace count (initial with 0 for other counts)
          add(Pair(
            HomeLoadsFilterItem(HomeLoadsFilterItemData(
              "Marketplace", 0, 0, 0, cachedMarketplaceCount, userPrefs.demandType
            )), AddUpdate
          ))
          
          if (!paginate) {
            if (userPrefs.verificationStatus.equals("failed")) {
              add(Pair(HomeLoadsKycPendingItem(), AddUpdate))
            } else {
              add(Pair(HomeMarketPlaceInfoItem(), AddUpdate))
            }
          }
          
          if (total == 0 && !paginate) {
            add(Pair(HomeLoadsWarningItem_NoLoads, AddUpdate))
          }
          
          for ((index, load) in loads.toMutableList().withIndex()) {
            try {
              load.transactionId?.let { txnIds.add(it) }
              
              // Find and set lowest bid
              val lowestBid = lowestBids.firstOrNull { b ->
                b.transactionId.safeEquals(load.transactionId)
              }
              lowestBid?.let {
                load.lowestBid = it.minBid
                load.numBids = it.numBids
              }
              load.loadPricePercent = loadPricePercent
              
              // Find and set transaction bid
              val transactionBid = bids.firstOrNull { b ->
                b.transactionId.safeEquals(load.transactionId)
              }
              load.transactionBid = transactionBid
              
            } catch (e: Exception) {
              Log.d("No Bid found for: ", load.transactionId ?: "")
            }
            
            populatePaymentFields(load)
            
            if (index.rem(HomeLoadsAddTruckItemDataConfig) == 0 && index != 0) {
              add(Pair(HomeLoadsAddTruckItem(), Add))
            }
            add(Pair(HomeLoadsMarketplaceItem(load), Add))
            Log.d("MarketplaceDebug", "Added marketplace item ${index + 1}: transactionId=${load.transactionId}")
          }
          
          if (!hasMoreData && !hasOrionLoadOnce) {
            add(Pair(HomeLoadsMoreInfoItem(), AddUpdate))
          }
          
          Log.d("MarketplaceDebug", "Total items to post: ${this.size}")
        }.let {
          Log.d("MarketplaceDebug", "Posting ${it.size} items to userLoadsData")
          userLoadsData.postValue(it)
          if (transactions?.isNotEmpty() == true && paginateCount == 0) {
            marketPlaceListShownTracked.postValue(true)
          }
        }
      }
      
      is Resource.Failure -> {
        mainTrace.stop()
        Log.e("HomeLoadsViewModel", "Error fetching marketplace data: ${dataResult.apiError}")
      }
      
      is Resource.Loading -> { /* not expected */ }
    }
  }

  /**
   * Checks if user has added routes/lane preference
   */
  fun checkUserRoutes() {
    compositeDisposable += userRepository.getUser(false)
        .onBackground()
        .subscribe { _user, error ->
          if (!error) {
            routesLiveData.postValue(_user.hasRoutes())
          } else {
            error.handle()
          }
        }
  }

    fun getUser() {
        compositeDisposable += userRepository.getUser(false)
            .onBackground()
            .subscribe { _user, error ->
                if (!error && _user != null) {
                    user = _user
                } else {
                    error?.handle()
                }
            }
    }
  fun fetchTruckType(data :HomeBidsRequestItemData) {
    compositeDisposable += truckRepository.getTruckType()
      .onBackground()
      .subscribe { _tRes, error ->
         if(!error && _tRes != null){
           truckGetLiveData.postValue(Pair(_tRes,data))
         }
        else{
          error.handle()
          truckGetLiveData.postValue(null)
         }
      }
  }

  override fun createBid(
    isPMT: Boolean,
    transactionId: String,
    bidAmount: Int,
    pmtRate: Int,
    commercialType: String,
    position: Int,
    expectedArrivalTimePickup:String,
    expectedArrivalTimePickupRemark:String
  ) {
    compositeDisposable += bidsRepository.createBid(
        isPMT, transactionId, bidAmount, pmtRate, commercialType,  expectedArrivalTimePickup, expectedArrivalTimePickupRemark,null
    )
        .delay(BidsUpdateDelay, SECONDS)
        .flatMap {
          bidsRepository.transactionBid(transactionId)
        }
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            bidsActionLiveData.postValue(Pair(position, _res))
          } else {
            error.handle()
          }
        }
  }

  override fun editBid(
    isPMT: Boolean,
    transactionId: String,
    bidId: String,
    bidAmount: Int,
    pmtRate: Int,
    commercialType: String,
    position: Int,
    expectedArrivalTimePickup:String,
    expectedArrivalTimePickupRemark:String
  ) {
    compositeDisposable += bidsRepository.editBid(
        isPMT, transactionId, bidId, bidAmount, commercialType, pmtRate,  expectedArrivalTimePickup, expectedArrivalTimePickupRemark,null
    )
        .delay(BidsUpdateDelay, SECONDS)
        .flatMap {
          bidsRepository.transactionBid(transactionId)
        }
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            bidsActionLiveData.postValue(Pair(position, _res))
          } else {
            error.handle()
          }
        }
  }

  /**
   * Update app user access status
   */
  fun updateUserAppAccess() {
    compositeDisposable += userRepository.updateUserAppAccess()
        .onBackground()
        .subscribe { _, _ -> }
  }

  /**
   * Update FCM token
   */
  fun updateFCMToken(fcmToken: String) {
    compositeDisposable += userRepository.updateFCMToken(fcmToken)
        .onBackground()
        .subscribe { _, error ->
          if (!error) {
            userPrefs.fcmTokenGenerated = false
            userPrefs.moengageFcmTokenGenerated = true
          }
        }
  }

  override fun reviseBid(transactionBid: TransactionBid?,position: Int) {
    reviseBidLiveData.postValue(Pair(true, position))
  }

  /**
   * Fetch lowest bid of a particular transaction
   */
  fun fetchLowestBid(transaction: HomeBidsRequestItemData, pos: Int) {
    compositeDisposable += bidsRepository.bulkLowestBidsForLoadsRx(listOf(transaction))
        .onBackground()
        .progress()
        .subscribe { res, error ->
          if (!error && res != null) {
            transaction.lowestBid = res.second[0].minBid
            transaction.numBids = res.second[0].numBids
            lowestBidLiveData.postValue(Pair(pos, transaction))
          } else {
            lowestBidLiveData.postValue(Pair(pos, transaction))
          }
        }
  }

  override fun createBids(
      transactionId: String,
      position: Int,
      createPayload: List<VehicleBidData>,
      unAllocatedLoad: Double
  ) {
    val bulkBidRequest= BulkBidCreateRequest("PMT","axle-app", userPrefs.userId(),unAllocatedLoad, userPrefs.userName,
            transactionId, createPayload)
    compositeDisposable += bidsRepository.createBulkBids(bulkBidRequest)
        .delay(BidsUpdateDelay, SECONDS)
        .flatMap {
            bidsRepository.transactionBidForBulk(transactionId)
        }
        .onBackground()
        .progress()
        .subscribe { _res, error ->
        if (!error && _res!=null) {
          bulkBidActionLiveData.postValue(Pair(position, _res))

        } else {
          error.handle()
          bulkBidActionLiveData.postValue(null)
        }
      }
  }

  override fun editBids(
    transactionId: String,
    position: Int,
    createPayload: List<VehicleBidData>,
    modifyPayload: List<ModifyVehicleData>,
    removedBids: List<String>,
    unAllocatedLoad: Double
  ) {
    var bulkBidCreateRequest : BulkBidCreateRequest? = null
    var bulkBidUpdateRequest: BulkBidUpdateRequest? = null
    var bulkBidRemoveRequest : BulkBidRemoveRequest? = null

    if(createPayload.isNotEmpty()) {
      bulkBidCreateRequest = BulkBidCreateRequest("PMT", "axle-app", userPrefs.userId(), unAllocatedLoad, userPrefs.userName,
              transactionId, createPayload)
    }
    if(modifyPayload.isNotEmpty()){
      bulkBidUpdateRequest = BulkBidUpdateRequest("PMT", "axle-app", userPrefs.userId(), unAllocatedLoad, "bid_update",
              transactionId, modifyPayload)
    }
    if(removedBids.isNotEmpty()){
      bulkBidRemoveRequest = BulkBidRemoveRequest("PMT", "axle-app", userPrefs.userId(), unAllocatedLoad, "bid_delete",
              "remove", transactionId, removedBids)
    }




    if ( bulkBidCreateRequest != null){
      compositeDisposable += bidsRepository.createBulkBids(bulkBidCreateRequest)
              .onBackground()
              .progress()
              .subscribe { _res, error ->
                if (!error && _res!=null) {
                  editFlg[0] = true
                  editBulkLiveData.postValue(Pair(10, transactionId))
                } else {
                  error.handle()
                  editFlg[0] = true
                  editBulkLiveData.postValue(Pair(11,transactionId))
                }
              }

    }
    else{
      editFlg[0] = true
    }
    if(bulkBidUpdateRequest != null) {
      compositeDisposable += bidsRepository.editBulkBid(bulkBidUpdateRequest)
              .onBackground()
              .progress()
              .subscribe { _res, error ->
                if (!error && _res != null) {
                  editFlg[1]= true
                  editBulkLiveData.postValue(Pair(20, transactionId))
                }
                else
                {
                  error.handle()
                  editFlg[1]= true
                  editBulkLiveData.postValue(Pair(21,transactionId))

                }
              }

    }
    else{
      editFlg[1]= true
    }

    if(bulkBidRemoveRequest != null){
      compositeDisposable+= bidsRepository.removeBulkBids(bulkBidRemoveRequest)
              .onBackground()
              .progress()
              .subscribe{_res, error ->
                if (!error && _res != null) {
                  editFlg[2]= true
                  editBulkLiveData.postValue(Pair(30, transactionId))
                }
                else{
                  error.handle()
                  editFlg[2]= true
                  editBulkLiveData.postValue(Pair(31,transactionId))

                }
              }

    }
    else
    {
      editFlg[2]= true
    }

  }

    fun transactionBidForBulk(transactionId: String,position: Int) {
        compositeDisposable += bidsRepository.transactionBidForBulk(transactionId)
                .onBackground()
                .progress()
                .subscribe { _res, error ->
                    if (!error && _res != null) {
                        bulkBidActionLiveData.postValue(Pair(position, _res))
                    } else {
                        error.handle()
                        bulkBidActionLiveData.postValue(null)
                    }
                }
    }

    override fun acceptBid(
        position: Int,
        transactionId: String,
        supplierId: String,
        supplierName: String,
        bidAmount: Int,
        commercialType: String,
        vehicleNumber: String,
        driverPhone:String,
        driverName: String
    ) {
        tripsRepository.acceptTripBid(transactionId,supplierId,supplierName,bidAmount,commercialType,vehicleNumber,driverPhone,driverName)
            .onBackground()
            .subscribe { _res, error ->
                if (!error && _res != null) {
                    acceptBidLiveData.postValue(Pair(position, _res))
                } else {
                    error.handle()
                    acceptBidLiveData.postValue(null)
                }
            }
    }
}



private const val BidsUpdateDelay = 1L

interface CloseDialog{
    fun dismissDialog()
}
/**
 * Filter demand types for recommendation API - only Internal and Others are valid
 * Filters out Intracity, Intracity_OPS, and Spot_Marketplace
 */
private fun filterDemandTypeForRecommendations(demandType:String): String {
    return if (demandType.contains(DemandType.Internal.type)) {
        "${DemandType.Internal.type},${DemandType.Others.type}"
    } else {
        DemandType.Others.type
    }
}