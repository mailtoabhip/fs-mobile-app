package com.delhivery.axle.ui.home.fragments.contracts

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.ContractType
import com.delhivery.axle.api.repository.DemandType
import com.delhivery.axle.api.repository.TransactionStatus.Requested
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.repository.UserTripsLoadLimit
import com.delhivery.axle.api.response.ContractsSummaryResponse
import com.delhivery.axle.api.response.LowestBidResponse
import com.delhivery.axle.api.response.TransactionsResponse
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.Quintuple
import com.delhivery.axle.data.Septuple
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.contracts.HomeContractsFilterItemData
import com.delhivery.axle.data.home.contracts.HomeContractsIntracityFilterItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.utils.extensions.isNotEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import io.reactivex.Single
import io.reactivex.functions.Function3
import io.reactivex.functions.Function5
import io.reactivex.schedulers.Schedulers

import javax.inject.Inject

class HomeContractsViewModel@Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val userRepository: UserRepository,
  private val bidsRepository: BidsRepository,
  val userPrefs: UserPrefs
) : BaseViewModel() {

  /* user bids live data */
  var userLoadsData =
    MutableLiveData<List<Pair<BaseHomeContractsRVAdapterItem<*>, DataRVAdapterOperationType>>>()
  var userLoadsDataFetch =
    MutableLiveData<List<Pair<BaseHomeContractsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  var bulkBidActionLiveData = MutableLiveData<Pair<Int,List<TransactionBid>>>()

  /* loads count live data */
  var contractsCountLiveData = MutableLiveData<Int>()

  /* data loading live data */
  var dataLoadingLiveData = MutableLiveData<Boolean>()

  var truckGetLiveData = MutableLiveData<Pair<List<TruckResponseArray>, HomeBidsRequestItemData>>()

  var loadPricePercent = 0

  /* pagination params */
  var hasMoreData = true
  var fecthToCalled = true
  var more_default_loads = false
  var vehicleTypes: String?= null
  var passing_vehicle_type: String?= null
  var filterVehicleType: Boolean?= null
  var offset = 0
  var total = 0
  var allActiveFetched = false
  var transactionIds:String?=null

  var hasOrionLoadOnce = false

  /* vehicle_type filter */
  var vehicleStr = userPrefs.truckTypes
  //var type = userPrefs.demandType

  var paginateCount =0

  var txnIds:ArrayList<String> = ArrayList()
  var searchAfterCreationTime :String? = null
  var searchAfterTransactionId : String? = null

  var fromNotification: Boolean
    get() = userPrefs.fromNotification
    set(value) {
      userPrefs.fromNotification = value
    }

  /**
   * Fetch user [Requested] transactions
   */
  fun fetchUserTransactions(
    paginate: Boolean = false, demandType: String,contractType: String?=null,isFlexible:Boolean?=null,includeFlexibleContracts:Boolean?=null){
    if (!paginate ) {
      searchAfterTransactionId = null
      searchAfterCreationTime = null
      offset = 0
      allActiveFetched = false
    } else if (paginate && !hasMoreData) {
      return
    }

    if (!paginate) {
      txnIds.clear()
    }

    if (paginate) {
      paginateCount += 1
      Pair(HomeContractsProgressItem(), AddUpdate).let { userLoadsData.postValue(listOf(it)) }
    }

    passing_vehicle_type = vehicleStr
    vehicleTypes = passing_vehicle_type


    dataLoadingLiveData.postValue(true)
    val mainTrace = Firebase.performance.newTrace("fetch_contract_transactions")
    val parallelTrace = Firebase.performance.newTrace("fetch_bids_for_contract_transactions_parallel")
    mainTrace.start()
    Log.d("cntrctVM", "srchaftr $searchAfterCreationTime searctTrans $searchAfterTransactionId")
    compositeDisposable += transactionsRepository.fetchContractsTransactions(offset, demandType, allActiveFetched = allActiveFetched,
      UserTripsLoadLimit,if(demandType==DemandType.Intracity.type)true else null,isFlexible,includeFlexibleContracts, searchAfterCreationTime = searchAfterCreationTime,searchAfterTransactionId= searchAfterTransactionId)
      .flatMap  { _res ->
        searchAfterTransactionId = _res.searchAfterTransactionId
        searchAfterCreationTime = _res.searchAfterCreationTime
        Log.d("cntrctVM", "srchaftr $searchAfterCreationTime searctTrans $searchAfterTransactionId")
        total = _res.total
        offset = _res.offset
        hasMoreData = _res.searchAfterTransactionId!= null && _res.searchAfterCreationTime !=null && _res.total!=0
        allActiveFetched = _res.allActiveFetched?:false
        parallelTrace.start()
        // Apply client-side vehicle filtering if needed
        val filteredTransactions = if (filterVehicleType == true && !vehicleStr.isNullOrEmpty()) {
          val selectedVehicleTypes = vehicleStr!!.split(",").map { it.trim() }
          _res.transactions?.filter { transaction ->
            transaction.truckType?.lowercase() in selectedVehicleTypes.map { it.lowercase() }
          }
        } else {
          _res.transactions
        }
        
        // Update the response with filtered transactions
        val filteredRes = _res.copy(transactions = filteredTransactions)
        
        Single.zip(
          bidsRepository.bidsForLoads(filteredTransactions,true).subscribeOn(Schedulers.io()),
          bidsRepository.bulkLowestBidsForLoads(filteredTransactions).subscribeOn(Schedulers.io()),
          transactionsRepository.fetchContractsSummaryCount().subscribeOn(Schedulers.io()),
          // Fetch intercity contracts for count calculation
          transactionsRepository.fetchContractsTransactions(
            0, 
            "${DemandType.Internal.type},${DemandType.Corporate.type}", 
            allActiveFetched = false,
            100,
            false,
            null,
            null,
            null,
            null
          ).subscribeOn(Schedulers.io()),
          // Fetch intracity contracts for count calculation
          transactionsRepository.fetchContractsTransactions(
            0, 
            DemandType.Intracity.type, 
            allActiveFetched = false,
            100,
            true,
            null,
            true,
            null,
            null
          ).subscribeOn(Schedulers.io()),
          Function5<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>, Pair<List<HomeBidsRequestItemData>, List<LowestBidResponse>>, ContractsSummaryResponse, TransactionsResponse, TransactionsResponse,
              Septuple<List<HomeBidsRequestItemData>, List<TransactionBid>, List<LowestBidResponse>,ContractsSummaryResponse,TransactionsResponse,TransactionsResponse,TransactionsResponse>> { t1, t2, t3, intercityRes, intracityRes ->
            Septuple(t1.first, t1.second, t2.second, t3, intercityRes, intracityRes, filteredRes)
          })
      }
      .onBackground()
      .subscribe { _tRes, error ->
        if(error != null) mainTrace.putAttribute("error_response_received", error.message.toString())
        mainTrace.stop()
        parallelTrace.stop()
        if (!error && _tRes != null) {
          mutableListOf<Pair<BaseHomeContractsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            /* remove progress item */
            add(Pair(HomeContractsProgressItem(), Remove))

            val loads = _tRes.first
            val bids = _tRes.second
            val bidTransactionIds = bids.map { it.transactionId }.toSet()
         /*   val loads = loadsWithBids.filter { load ->
              load.transactionId !in bidTransactionIds
            }*/

            add(Pair(HomeContractsSearchItem(), AddUpdate))
              
              // Get intercity and intracity data from the API responses
              val intercityTransactions = _tRes.fifth.transactions
              val intracityTransactions = _tRes.sixth.transactions
              
              // Apply truck filter to both datasets
              val filteredIntercityTransactions = if (filterVehicleType == true && !vehicleStr.isNullOrEmpty()) {
                val selectedVehicleTypes = vehicleStr!!.split(",").map { it.trim() }
                intercityTransactions?.filter { transaction ->
                  transaction.truckType?.lowercase() in selectedVehicleTypes.map { it.lowercase() }
                }
              } else {
                intercityTransactions
              }
              
              val filteredIntracityTransactions = if (filterVehicleType == true && !vehicleStr.isNullOrEmpty()) {
                val selectedVehicleTypes = vehicleStr!!.split(",").map { it.trim() }
                intracityTransactions?.filter { transaction ->
                  transaction.truckType?.lowercase() in selectedVehicleTypes.map { it.lowercase() }
                }
              } else {
                intracityTransactions
              }
              
              // Calculate counts from filtered data
              // Express = LH_FTL contracts, Non-Express = FRC contracts
              val expressCount = filteredIntercityTransactions?.count { it.contractType == ContractType.LH_FTL.type }
              val nonExpressCount = filteredIntercityTransactions?.count { it.contractType == ContractType.FRC.type }
              val intraCityCount = filteredIntracityTransactions?.size
              
              // Calculate total active count for legacy purposes
              var totalActive = 0
              for(item in _tRes.fourth.contractsCount.active){
                if(userPrefs.demandType.contains(DemandType.Internal.type)&& userPrefs.demandType.contains(DemandType.Intracity.type)&& userPrefs.contractDemand) {
                  if(item.key==ContractType.LH_FTL.type){
                    totalActive+=item.count?:0
                  }
                  if(item.key==ContractType.FRC.type){
                    totalActive+=item.count?:0
                  }
                  if(item.key==ContractType.INTRACITY.type){
                    totalActive+=item.count?:0
                  }
                }else if(userPrefs.demandType.contains(DemandType.Internal.type)&&userPrefs.contractDemand){
                  if(item.key==ContractType.LH_FTL.type){
                    totalActive+=item.count?:0
                  }
                  if(item.key==ContractType.FRC.type){
                    totalActive+=item.count?:0
                  }

                }else if(userPrefs.demandType.contains(DemandType.Intracity.type)&&userPrefs.contractDemand){
                  if(item.key==ContractType.INTRACITY.type){
                    totalActive+=item.count?:0
                  }
                  if(userPrefs.demandType.contains(DemandType.Others.type)){
                    if(item.key==ContractType.FRC.type){
                      totalActive+=item.count?:0
                    }
                  }
                }else{
                  if(item.key==ContractType.FRC.type){
                    totalActive+=item.count?:0
                  }
                }
              }

            val count = (expressCount ?: 0) + (nonExpressCount ?: 0) + (intraCityCount ?: 0)
            Log.d("viewmodelContract", "Filtered counts - expressCount: ${expressCount}, intraCityCount: ${intraCityCount}, nonExpressCount: ${nonExpressCount}")

            contractsCountLiveData.postValue(count)
              add(Pair(HomeContractsFilterItem(HomeContractsFilterItemData(demandType, expressCount ?: 0, nonExpressCount ?: 0, intraCityCount ?: 0, userPrefs.demandType, userPrefs.contractDemand)), AddUpdate))
             // Handle filter for intracity contract type


            if(demandType==DemandType.Intracity.type){
                var intracityContractType = ""
                intracityContractType = if(contractType==ContractType.INTRACITY.type && isFlexible==true){
                  "Flexible"
                }else if(contractType==ContractType.INTRACITY.type && isFlexible==false){
                  "Fixed"
                }else "All"
                //add(Pair(HomeContractsIntracityFilterItem(HomeContractsIntracityFilterItemData(intracityContractType)), AddUpdate))
              }
            //  loadsCountLiveData.postValue(totalActive)
             if (_tRes.seventh.transactions?.isEmpty() != false && !paginate) {
               add(Pair(HomeContractsWarningItem_NoLoads, AddUpdate))
              }
              if(_tRes.seventh.transactions?.isNotEmpty() == true){
              for ((index, load) in loads.toMutableList().withIndex()) {
                try {
                  load.transactionId?.let { txnIds.add(it) }

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

                } catch (e: Exception) {
                  Log.d("No Bid found for: ", load.transactionId ?: "")
                }
                add(Pair(HomeContractsRequestItem(load), Add))
              }

              }

          }.let { userLoadsData.postValue(it) }
        }else{
          mutableListOf<Pair<BaseHomeContractsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            /* remove progress item */
            add(Pair(HomeContractsProgressItem(), Remove))
            /* add api time out item */
            add(Pair(HomeContractsWarningItem_TimeOut, AddUpdate))
          }
            .let {
              if(!userLoadsData.value?.isNullOrEmpty()){
                if(userLoadsData.value!=null && userLoadsData.value!!.size>0){
                  it.addAll(userLoadsData.value!!)}
              }
              userLoadsDataFetch.postValue(it) }
        }

        dataLoadingLiveData.postValue(false)
      }
  }

}



private const val BidsUpdateDelay = 1L