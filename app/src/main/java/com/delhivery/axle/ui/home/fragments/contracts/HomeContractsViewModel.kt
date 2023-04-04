package com.delhivery.axle.ui.home.fragments.contracts

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.TransactionStatus.Requested
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.repository.UserTripsLoadLimit
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.SixTuple
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.contracts.HomeContractsFilterItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.Single

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
    paginate: Boolean = false, demandType: String,
    isInternal: Boolean = false) {
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

    compositeDisposable += transactionsRepository.fetchContractsTransactions(offset, demandType, allActiveFetched = allActiveFetched,
        UserTripsLoadLimit)
      .flatMap  { _res ->
        total = _res.total
        offset = _res.offset
        hasMoreData = _res.hasNext
        allActiveFetched = _res.allActiveFetched?:false
        var oppositeDemandType = if(demandType=="Internal"){ "Corporate" }else{ "Internal" }
        Single.zip(
          bidsRepository.bidsForLoads(_res.transactions,true),
          bidsRepository.bulkLowestBidsForLoads(_res.transactions),
          transactionsRepository.fetchContractsTransactions(0, oppositeDemandType,null,1,"yes"),
          transactionsRepository.fetchContractsTransactions(0, demandType,null,1,"yes"),
          ) { t1, t2, t3, t4 ->
          SixTuple(t1.first, t1.second, t2.second, t3, _res,t4)
        }
      }
      .onBackground()
      .subscribe { _tRes, error ->
        if (!error && _tRes != null) {
          mutableListOf<Pair<BaseHomeContractsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            /* remove progress item */
            add(Pair(HomeContractsProgressItem(), Remove))

            val loads = _tRes.first
            val bids = _tRes.second

            if (_tRes.fourth.transactions.isEmpty() && _tRes.fifth.transactions.isEmpty()) {
              add(Pair(HomeContractsWarningItem_NoLoads, AddUpdate))
            } else {
              add(Pair(HomeContractsSearchItem(), AddUpdate))
              var totalActive=0
              var expressCount =0
              var nonExpressCount =0
              if(userPrefs.demandType.contains("Internal")){
                if(_tRes.fourth.activeCount!=null && _tRes.sixth.activeCount!=null){
                  totalActive = _tRes.fourth.activeCount+_tRes.sixth.activeCount
                }
              }else{
                val contractType = "FRC"
                if(_tRes.fourth.activeCount!=null && _tRes.fourth.transactions.isNotEmpty()&& _tRes.fourth.transactions.isNotEmpty() &&_tRes.fourth.transactions[0].contractType==contractType){
                  totalActive = _tRes.fourth.activeCount
                }
                else if(_tRes.sixth.activeCount!=null && _tRes.sixth.transactions.isNotEmpty()&& _tRes.sixth.transactions.isNotEmpty() &&_tRes.sixth.transactions[0].contractType==contractType){
                  totalActive = _tRes.sixth.activeCount
                }

              }
               if(_tRes.fourth.transactions.isNotEmpty()){
                 if(_tRes.fourth.transactions.get(0).isItLHContract()&& userPrefs.demandType.contains("Internal")){
                   expressCount = _tRes.fourth.total
                 }else{
                   nonExpressCount = _tRes.fourth.total
                 }
               }
              if(_tRes.sixth.transactions.isNotEmpty()){
                if(_tRes.sixth.transactions.get(0).isItLHContract()&& userPrefs.demandType.contains("Internal")){
                  expressCount = _tRes.sixth.total
                }else{
                  nonExpressCount = _tRes.sixth.total
                }
              }
              add(Pair(HomeContractsFilterItem(HomeContractsFilterItemData("",isInternal, expressCount ,nonExpressCount,0,userPrefs.demandType)), AddUpdate))

              loadsCountLiveData.postValue(totalActive)
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