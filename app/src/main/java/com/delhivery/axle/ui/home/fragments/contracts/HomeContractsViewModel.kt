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
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.contracts.HomeContractsFilterItemData
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
import io.reactivex.Single
import io.reactivex.functions.Function3

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
  var loadsCountLiveData = MutableLiveData<Int>()

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


  var fromNotification: Boolean
    get() = userPrefs.fromNotification
    set(value) {
      userPrefs.fromNotification = value
    }

  /**
   * Fetch user [Requested] transactions
   */
  fun fetchUserTransactions(
    paginate: Boolean = false, demandType: String) {
    if (!paginate ) {
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

    val originCityList= ArrayList<String>()
    if(userPrefs.getLanesPreference()!=null ){
      for(item in userPrefs.getLanesPreference()!!){
        originCityList?.add(item?.origin?.orion_db_city_code?:"")
      }
    }
    compositeDisposable += transactionsRepository.fetchContractsTransactions(offset, demandType, allActiveFetched = allActiveFetched,
        5,if(originCityList.isNotEmpty()&& demandType==DemandType.Intracity.type)originCityList?.joinToString(separator = ",")else null)
      .flatMap  { _res ->
        total = _res.total
        offset = _res.offset
        hasMoreData = _res.hasNext
        allActiveFetched = _res.allActiveFetched?:false
        Single.zip(
          bidsRepository.bidsForLoads(_res.transactions,true),
          bidsRepository.bulkLowestBidsForLoads(_res.transactions),
          transactionsRepository.fetchContractsSummaryCount(if(originCityList.isNotEmpty())originCityList?.joinToString(separator = ",")else null),
          Function3<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>, Pair<List<HomeBidsRequestItemData>, List<LowestBidResponse>>, ContractsSummaryResponse,
              Quintuple<List<HomeBidsRequestItemData>, List<TransactionBid>, List<LowestBidResponse>,ContractsSummaryResponse,TransactionsResponse>> { t1, t2,t3 ->
            Quintuple(t1.first, t1.second, t2.second,t3,_res)
          })
      }
      .onBackground()
      .subscribe { _tRes, error ->
        if (!error && _tRes != null) {
          mutableListOf<Pair<BaseHomeContractsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            /* remove progress item */
            add(Pair(HomeContractsProgressItem(), Remove))

            val loads = _tRes.first
            val bids = _tRes.second
              add(Pair(HomeContractsSearchItem(), AddUpdate))
              // calculating the count based on user demand type
              var totalActive=0
              var expressCount =0
              var nonExpressCount =0
              var intraCityCount =0

                for(item in _tRes.fourth.contractsCount.all){
                  if(userPrefs.demandType.contains(DemandType.Internal.type)&& userPrefs.demandType.contains(DemandType.Intracity.type)&& userPrefs.contractDemand) {
                    if (item.key == ContractType.LH_FTL.type) {
                      expressCount = item.count ?: 0
                    }
                    if (item.key == ContractType.FRC.type) {
                      nonExpressCount = item.count ?: 0
                    }
                    if (item.key == ContractType.INTRACITY.type) {
                      intraCityCount = item.count ?: 0
                    }
                  }else if(userPrefs.demandType.contains(DemandType.Internal.type) && userPrefs.contractDemand){
                    if(item.key==ContractType.LH_FTL.type){
                      expressCount =item.count?:0
                    }
                    if(item.key==ContractType.FRC.type){
                      nonExpressCount=item.count?:0
                    }

                  }else if(userPrefs.demandType.contains(DemandType.Intracity.type)&& userPrefs.contractDemand){
                    if(item.key==ContractType.INTRACITY.type){
                      intraCityCount = item.count?:0
                    }
                    if(userPrefs.demandType.contains(DemandType.Others.type)){
                      if(item.key==ContractType.FRC.type){
                        nonExpressCount=item.count?:0
                      }
                    }
                }else{
                  if(item.key==ContractType.FRC.type){
                    nonExpressCount=item.count?:0
                  }
              }}
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

              add(Pair(HomeContractsFilterItem(HomeContractsFilterItemData(demandType, expressCount ,nonExpressCount, intraCityCount,userPrefs.demandType,userPrefs.contractDemand)), AddUpdate))

              loadsCountLiveData.postValue(totalActive)
             if (_tRes.fifth.transactions.isEmpty()) {
               add(Pair(HomeContractsWarningItem_NoLoads, AddUpdate))
              }
              if(_tRes.fifth.transactions.isNotEmpty()){
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