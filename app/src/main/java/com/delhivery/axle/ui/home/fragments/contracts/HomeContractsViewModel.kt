package com.delhivery.axle.ui.home.fragments.contracts

import android.util.Log
import android.view.SurfaceControl.Transaction
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.TransactionStatus.Requested
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.repository.TruckRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.response.LowestBidResponse
import com.delhivery.axle.api.response.TransactionsResponse
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.Quintuple
import com.delhivery.axle.data.bids.BulkBidCreateRequest
import com.delhivery.axle.data.bids.BulkBidRemoveRequest
import com.delhivery.axle.data.bids.BulkBidUpdateRequest
import com.delhivery.axle.data.bids.ModifyVehicleData
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.VehicleBidData
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.contracts.HomeContractsFilterItemData
import com.delhivery.axle.data.home.loads.HomeLoadsAddTruckItemDataConfig
import com.delhivery.axle.data.home.loads.HomeLoadsFilterItemData
import com.delhivery.axle.data.home.loads.HomeLoadsSummaryItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialogInterface
import com.delhivery.axle.ui.biddetails.BulkBidsCreateEditInterface
import com.delhivery.axle.ui.dialogs.BidConfirmReviseDialogInterface
import com.delhivery.axle.ui.home.fragments.loads.BaseHomeLoadsRVAdapterItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsAddTruckItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsFilterItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsInfoItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsMoreInfoItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsProgressItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRequestItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsSearchItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsSummaryItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsTruckPriorityAccessItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsWarningItem_NoLoads
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsWarningItem_TimeOut
import com.delhivery.axle.ui.home.fragments.loads_truck.UpdateTabCountAndBadgeInterface
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.lang.reflect.Constructor
import java.util.concurrent.TimeUnit.SECONDS
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

    compositeDisposable += transactionsRepository.fetchContractsTransactions(0, demandType)
      .flatMap  { _res ->
        total = _res.total
        offset = _res.offset
        total = _res.total
        hasMoreData = _res.offset!=_res.total
        fecthToCalled =_res.offset<_res.total
        loadPricePercent = _res.loadPricePercent
        more_default_loads = _res.more_loads
        var oppositeDemandType = if(demandType=="Internal"){ "Corporate" }else{ "Internal" }
        Single.zip(
          bidsRepository.bidsForLoads(_res.transactions,true),
          bidsRepository.bulkLowestBidsForLoads(_res.transactions),
          transactionsRepository.fetchContractsTransactions(0, oppositeDemandType),
          Function3<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>, Pair<List<HomeBidsRequestItemData>, List<LowestBidResponse>>,TransactionsResponse,
              Quintuple<List<HomeBidsRequestItemData>, List<TransactionBid>, List<LowestBidResponse>,TransactionsResponse,TransactionsResponse>> { t1, t2,t3 ->
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

            if (total == 0 && (isInternal==false)) {
              add(Pair(HomeContractsWarningItem_NoLoads, Add))
            } else {
              var totalActive=0
              var expressCount =0
              var nonExpressCount =0
              if(_tRes.fourth.activeCount!=null && _tRes.fifth.activeCount!=null){
                totalActive = _tRes.fourth.activeCount+_tRes.fifth.activeCount
              }
               if(_tRes.fourth.transactions.size>0){
                 if(_tRes.fourth.transactions.get(0).isItLHContract()){
                   expressCount = _tRes.fourth.total
                 }else{
                   nonExpressCount = _tRes.fourth.total
                 }
               }
              if(_tRes.fifth.transactions.size>0){
                if(_tRes.fifth.transactions.get(0).isItLHContract()){
                  expressCount = _tRes.fifth.total
                }else{
                  nonExpressCount = _tRes.fifth.total
                }
              }
              add(Pair(HomeContractsFilterItem(HomeContractsFilterItemData(isInternal, expressCount ,nonExpressCount)), AddUpdate))

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