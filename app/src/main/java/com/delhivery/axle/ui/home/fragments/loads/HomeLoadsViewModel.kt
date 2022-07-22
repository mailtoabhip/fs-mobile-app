package com.delhivery.axle.ui.home.fragments.loads

import android.os.Handler
import android.util.Log
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
import com.delhivery.axle.data.bids.*
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.loads.HomeLoadsAddTruckItemData
import com.delhivery.axle.data.home.loads.HomeLoadsAddTruckItemDataConfig
import com.delhivery.axle.data.home.loads.HomeLoadsFilterItemData
import com.delhivery.axle.data.home.loads.HomeLoadsSummaryItemData
import com.delhivery.axle.exception.NoBidsFoundException
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialogInterface
import com.delhivery.axle.ui.biddetails.BulkBidsCreateEditInterface
import com.delhivery.axle.ui.dialogs.BidConfirmReviseDialogInterface
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

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
  val userPrefs: UserPrefs
) : BaseViewModel(), BidDetailsCreateEditDialogInterface, BidConfirmReviseDialogInterface, BulkBidsCreateEditInterface {

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

  var lowestBidLiveData = MutableLiveData<Pair<Int, HomeBidsRequestItemData>>()

  /* revise bid live data */
  var reviseBidLiveData = MutableLiveData<Pair<Boolean, Int>>()

  /* loads count live data */
  var loadsCountLiveData = MutableLiveData<Int>()

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
  var transactionIds:String?=null

  var hasOrionLoadOnce = false

  /* vehicle_type filter */
  var vehicleStr = userPrefs.truckTypes
  //var type = userPrefs.demandType

  var paginateCount =0

    var txnIds:ArrayList<String> = ArrayList()


  var editBulkLiveData= MutableLiveData<Pair<Int,String>>()
  var editFlg= mutableListOf<Boolean>(false,false,false)


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
            isInternal: Boolean = false, infoSearch: Boolean = false, excludeTruckTypes: String?= null) {
        if (!paginate || infoSearch) {
            offset = 0
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

        compositeDisposable += transactionsRepository.fetchRecommTransactions(0, demandType, vehicleTypes, excludeTruckTypes, filterVehicleType, true)
                .flatMap  { _res ->
                    total = _res.total
                        offset = _res.offset
                        total = _res.total
                        hasMoreData = _res.offset!=_res.total
                        fecthToCalled =_res.offset<_res.total
                        loadPricePercent = _res.loadPricePercent
                        more_default_loads = _res.more_loads
                        loadsCountLiveData.postValue(total)

                        Single.zip(
                                bidsRepository.bidsForLoads(_res.transactions),
                                bidsRepository.bulkLowestBidsForLoads(_res.transactions),
                                BiFunction<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>, Pair<List<HomeBidsRequestItemData>, List<LowestBidResponse>>,
                                        Triple<List<HomeBidsRequestItemData>, List<TransactionBid>, List<LowestBidResponse>>> { t1, t2 ->
                                    Triple(t1.first, t1.second, t2.second)
                                })
                }
                .onBackground()
                .subscribe { _tRes, error ->
                    if (!error && _tRes != null) {
                        mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                            /* remove progress item */
                            add(Pair(HomeLoadsProgressItem(), Remove))

                            val loads = _tRes.first
                            val bids = _tRes.second

                            if (total == 0 && !infoSearch && (filterVehicleType == false) && (isInternal==false)) {
                                add(Pair(HomeLoadsWarningItem_NoLoads, Add))
                            } else {
                                add(Pair(HomeLoadsSearchItem(), AddUpdate))
                                add(Pair(HomeLoadsFilterItem(HomeLoadsFilterItemData(isInternal)), AddUpdate))
                                if(!paginate) {
                                    add(Pair(HomeLoadsTruckPriorityAccessItem(), AddUpdate))
                                }
                                add(Pair(HomeLoadsSummaryItem(HomeLoadsSummaryItemData(total)), AddUpdate))
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
                                        if (load.isDMTIndent()) {
                                            load.bulkTransactionBids =
                                                    bids.filter { b ->
                                                        b.transactionId.safeEquals(load.transactionId)
                                                    }
                                        }
                                    } catch (e: Exception) {
                                        Log.d("No Bid found for: ", load.transactionId ?: "")
                                    }
                                    if (index.rem(HomeLoadsAddTruckItemDataConfig) == 0 && index != 0) {
                                        add(Pair(HomeLoadsAddTruckItem(), Add))
                                    }
                                    add(Pair(HomeLoadsRequestItem(load), Add))
                                }

                                if (!hasMoreData && !hasOrionLoadOnce && more_default_loads) {
                                    add(Pair(HomeLoadsInfoItem(), AddUpdate))
                                }
                                add(Pair(HomeLoadsMoreInfoItem(), AddUpdate))
                            }
                        }.let { userLoadsData.postValue(it) }

                        if(!fecthToCalled) {
                          hasMoreData=true
                          fetchSupplierTransactions(
                              true, demandType, isInternal, infoSearch, excludeTruckTypes
                          )
                        }
                    } else {
//                        mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
//                            /* remove progress item */
//                            add(Pair(HomeLoadsProgressItem(), Remove))
//                            /* add api time out item */
//                            add(Pair(HomeLoadsWarningItem_TimeOut, AddUpdate))
//                        }
//                                .let { userLoadsData.postValue(it) }
                        fetchSupplierTransactions(true, demandType, isInternal, infoSearch, excludeTruckTypes)

                    }

                    dataLoadingLiveData.postValue(false)
                }
    }


    /**
   * Fetch user [Requested] transactions
   */
  fun fetchSupplierTransactions(
    paginate: Boolean = false, demandType: String,
    isInternal: Boolean = false, infoSearch: Boolean = false, excludeTruckTypes: String?= null) {
    if (!paginate || infoSearch) {
      offset = 0
    } else if (paginate && !hasMoreData ) {
      return
    }

    if (paginate) {
      paginateCount += 1
      Pair(HomeLoadsProgressItem(), AddUpdate).let { userLoadsDataFetch.postValue(listOf(it)) }
    }
      val distinct = txnIds.toSet().toList();
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

      compositeDisposable += transactionsRepository.fetchLoadBoardTransactions(offset, demandType, vehicleTypes, excludeTruckTypes, filterVehicleType, true, transactionIds)
                              .flatMap { t ->
                                  offsetFetch = t.offset
                                  total = total+t.total
                                  hasMoreData = t.offset != t.total
                                  loadPricePercent = t.loadPricePercent
                                  more_default_loads = t.more_loads
                                  loadsCountLiveData.postValue(total)

                                  Single.zip(
                                          bidsRepository.bidsForLoads(t.transactions),
                                          bidsRepository.bulkLowestBidsForLoads(t.transactions),
                                          BiFunction<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>, Pair<List<HomeBidsRequestItemData>, List<LowestBidResponse>>,
                                                  Triple<List<HomeBidsRequestItemData>, List<TransactionBid>, List<LowestBidResponse>>> { t1, t2 ->
                                              Triple(t1.first, t1.second, t2.second)
                                          })
                              }
        .onBackground()
        .subscribe { _tRes, error ->
          if (!error && _tRes != null) {
            mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeLoadsProgressItem(), Remove))

              val loads = _tRes.first
              val bids = _tRes.second

              if (total == 0 && !infoSearch && (filterVehicleType == false) && (isInternal==false)) {
                  add(Pair(HomeLoadsWarningItem_NoLoads, Add))
                } else {
                  add(Pair(HomeLoadsSearchItem(), AddUpdate))
                  add(Pair(HomeLoadsFilterItem(HomeLoadsFilterItemData(isInternal)), AddUpdate))
                  if(!paginate) {
                      add(Pair(HomeLoadsTruckPriorityAccessItem(), AddUpdate))
                  }
                  add(Pair(HomeLoadsSummaryItem(HomeLoadsSummaryItemData(total)), AddUpdate))
                  for ((index, load) in loads.toMutableList().withIndex()) {
                      try {
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
                      }
                      if (index.rem(HomeLoadsAddTruckItemDataConfig) == 0 && index != 0) {
                          add(Pair(HomeLoadsAddTruckItem(), Add))
                      }
                      add(Pair(HomeLoadsRequestItem(load), Add))
                  }

                  if (!hasMoreData && !hasOrionLoadOnce && more_default_loads) {
                      add(Pair(HomeLoadsInfoItem(), AddUpdate))
                  }
                  add(Pair(HomeLoadsMoreInfoItem(), AddUpdate))
              }
            }
                .let {
                  if(!userLoadsData.value?.isNullOrEmpty()){
                    it.addAll(userLoadsData.value!!)
                  }
                  userLoadsDataFetch.postValue(it) }

          } else {
            mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeLoadsProgressItem(), Remove))
              /* add api time out item */
              add(Pair(HomeLoadsWarningItem_TimeOut, AddUpdate))
            }
                .let {
                  if(!userLoadsData.value?.isNullOrEmpty()){
                    it.addAll(userLoadsData.value!!)
                  }
                  userLoadsDataFetch.postValue(it) }
          }

          dataLoadingLiveData.postValue(false)
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
        isPMT, transactionId, bidAmount, pmtRate, commercialType,  expectedArrivalTimePickup, expectedArrivalTimePickupRemark
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
        isPMT, transactionId, bidId, bidAmount, commercialType, pmtRate,  expectedArrivalTimePickup, expectedArrivalTimePickupRemark
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
          }
        }
  }

  override fun reviseBid(position: Int) {
    reviseBidLiveData.postValue(Pair(true, position))
  }

  /**
   * Fetch lowest bid of a particular transaction
   */
  fun fetchLowestBid(transaction: HomeBidsRequestItemData, pos: Int) {
    compositeDisposable += bidsRepository.bulkLowestBidsForLoads(listOf(transaction))
        .onBackground()
        .progress()
        .subscribe { res, error ->
          if (!error && res != null) {
            transaction.lowestBid = res.second[0].minBid
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
}



private const val BidsUpdateDelay = 1L