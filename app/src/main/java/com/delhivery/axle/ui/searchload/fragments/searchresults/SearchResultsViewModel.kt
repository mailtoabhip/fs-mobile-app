package com.delhivery.axle.ui.searchload.fragments.searchresults

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.repository.TripsRepository
import com.delhivery.axle.api.repository.TruckRepository
import com.delhivery.axle.api.repository.UserTripsLoadLimit
import com.delhivery.axle.api.response.LowestBidResponse
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.bids.*
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.*
import com.delhivery.axle.ui.biddetails.AcceptAdhocIntracityBidBottomDialogInterface
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialogInterface
import com.delhivery.axle.ui.biddetails.BulkBidsCreateEditInterface
import com.delhivery.axle.ui.dialogs.BidConfirmReviseDialogInterface
import com.delhivery.axle.ui.home.fragments.bids.SearchContractWarningItem_NoLoad
import com.delhivery.axle.ui.home.fragments.bids.SearchLoadWarningItem_NoLoad
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

/**
 * View model for [SearchResultsFragment]
 */
class SearchResultsViewModel @Inject constructor(
    private val transactionsRepository: TransactionsRepository,
    private val bidsRepository: BidsRepository,
    private val truckRepository: TruckRepository,
    private val tripsRepository: TripsRepository,
    val userPrefs: UserPrefs
) : BaseViewModel(), BidDetailsCreateEditDialogInterface, BulkBidsCreateEditInterface,BidConfirmReviseDialogInterface,AcceptAdhocIntracityBidBottomDialogInterface {

  /* bid action result live data */
  var bidsActionLiveData = MutableLiveData<Pair<Int, TransactionBid>>()

  /* search results live data */
  var searchResults = MutableLiveData<List<Pair<BaseSearchLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>>()
  var lowestBidLiveData = MutableLiveData<Pair<Int, HomeBidsRequestItemData>>()
  var truckGetLiveData = MutableLiveData<Pair<List<TruckResponseArray>,HomeBidsRequestItemData>>()
  var acceptBidLiveData = MutableLiveData<Pair<Int,Any>>()

    var editBulkLiveData= MutableLiveData<Pair<Int,String>>()
    var editFlg= mutableListOf<Boolean>(false,false,false)
    var bulkBidActionLiveData = MutableLiveData<Pair<Int,List<TransactionBid>>>()
    var loadPricePercent = 0
    var total =0
    var bidsCount =0

  /* revise bid live data */
  var reviseBidLiveData = MutableLiveData<Pair<Boolean, Int>>()
  var paginateCount=0
  /* data loading live data */
  var dataLoadingLiveData = MutableLiveData<Boolean>()

  /* pagination params */
  var offset=0
  var hasMoreData=false

  /**
   * Search load api
   */
  fun searchLoad(
    paginate:Boolean=false,
    origin: CityModel,
    destination: CityModel?,
    type: String?,
    displayName: String?,
    status: String?,
    requestType:String?,
    contractType:String?,
    isFlexible:Boolean?=null,
    includeFlexibleContracts:Boolean?=null
  ) {
    if(!paginate){
      offset=0
    }
    else if(paginate && !hasMoreData){
      return
    }
    if(paginate){
      paginateCount++
      if(requestType=="load")
        Pair(SearchLoadsProgressItem(), AddUpdate).let{ searchResults.postValue(listOf(it))}
      else
        Pair(SearchContractsProgressItem(), AddUpdate).let{ searchResults.postValue(listOf(it))}

    }

    dataLoadingLiveData.postValue(true)
    /* dummy data */
    val mainTrace = Firebase.performance.newTrace("search_loads")
    val parallelTrace = Firebase.performance.newTrace("fetch_bids_for_search_loads_parallel")
    mainTrace.start()
    compositeDisposable += transactionsRepository.searchTransactions(
        offset, origin.orionDbCityCode, destination?.orionDbCityCode, type?.lowercase(),displayName,status,requestType,contractType,
      UserTripsLoadLimit,isFlexible,includeFlexibleContracts
    )
        .flatMap { t ->
            parallelTrace.start()
          this.total=t.total
          this.hasMoreData=t.hasNext
          this.offset=t.offset
          this.loadPricePercent = t.loadPricePercent
          Single.zip(
            bidsRepository.bidsForLoads(t.transactions, requestType=="contract").subscribeOn(Schedulers.io()),
            bidsRepository.bulkLowestBidsForLoads(t.transactions).subscribeOn(Schedulers.io()),
            BiFunction<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>, Pair<List<HomeBidsRequestItemData>, List<LowestBidResponse>>,
                Triple<List<HomeBidsRequestItemData>, List<TransactionBid>, List<LowestBidResponse>>> { t1, t2 ->
              Triple(t1.first, t1.second, t2.second)
            })
        }
        .onBackground()
        .subscribe { _tRes, error ->
          if(error != null) mainTrace.putAttribute("error_response_received", error.message.toString())
          parallelTrace.stop()
          mainTrace.stop()
          if (!error && _tRes!=null) {
            mutableListOf<Pair<BaseSearchLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply{
              if(requestType=="load")
              add(Pair(SearchLoadsProgressItem(), Remove))
              else add(Pair(SearchContractsProgressItem(), Remove))
              val loads = _tRes.first
              val bids = _tRes.second
              total=loads.size
              bidsCount = bids.size
              for (load in loads.toMutableList()) {
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
                  load.transactionId?.let { Log.d("No Bid found for: ", it) }
                }
                if(load.isItContract())
                  add(Pair(SearchContractsRequestItem(load),Add))
                else add(Pair(SearchLoadsRequestItem(load),Add))
              }
              if(loads.size==0){
              if(requestType=="load")
                add(Pair(SearchLoadWarningItem_NoLoad, Add))
              else
                add(Pair(SearchContractWarningItem_NoLoad, Add))
              }
            }.let { searchResults.postValue(it) }
          } else {
            mutableListOf<Pair<BaseSearchLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply{
              if(!paginate){
                if(requestType=="load")
                  add(Pair(SearchLoadWarningItem_NoLoad, Add))
                else
                  add(Pair(SearchContractWarningItem_NoLoad, Add))
              }
            }.let{ searchResults.postValue(it)}
            error.handle()
          }
          dataLoadingLiveData.postValue(false)
        }
  }

  override fun createBid(
    isPMT: Boolean,
    transactionId: String,
    bidAmount: Int,
    pmtRate: Int,
    commercialType: String,
    position: Int, expectedArrivalTimePickup:String,
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
        isPMT, transactionId, bidId, bidAmount, commercialType,
        pmtRate,  expectedArrivalTimePickup,
            expectedArrivalTimePickupRemark,null
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
          transaction.numBids = res.second[0].numBids
          lowestBidLiveData.postValue(Pair(pos, transaction))
        } else {
          lowestBidLiveData.postValue(Pair(pos, transaction))
        }
      }
  }

  override fun reviseBid(transactionBid: TransactionBid?, position: Int) {
    reviseBidLiveData.postValue(Pair(true, position))
  }

    override fun acceptBid(
        position: Int,
        transactionId: String,
        supplierId: String,
        supplierName: String,
        bidAmount: Int,
        commercialType: String,
        vehicleNumber: String,
        driverPhone: String,
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