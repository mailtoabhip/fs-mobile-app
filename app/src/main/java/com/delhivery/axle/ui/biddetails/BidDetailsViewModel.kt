package com.delhivery.axle.ui.biddetails

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.*
import com.delhivery.axle.api.request.WarehouseRequest
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.api.response.WarehouseIndentResponse
import com.delhivery.axle.data.biddetail.BulkBidSummaryItemData
import com.delhivery.axle.data.bids.*
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Cancelled
import com.delhivery.axle.data.bids.TransactionBidStatus.Rejected
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.Single
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

/**
 * View model for [BidDetailsActivity]
 */
class BidDetailsViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val bidsRepository: BidsRepository,
  private val tripsRepository: TripsRepository,
  private val truckRepository: TruckRepository,
  private val warehouseRepository: WarehouseRepository,
  val userPrefs: UserPrefs
) : BaseViewModel(), BidDetailsCreateEditDialogInterface, BulkBidsCreateEditInterface{

  /* transaction id */
  lateinit var transactionId: String
    lateinit var dmtStatus: String
    var fromPage: Boolean = false
    var active = false
  /* live data */
  var transactionLiveData = MutableLiveData<HomeBidsRequestItemData>()

  var transactionBidLiveData = MutableLiveData<BidDetailsUserBidState>()

  var bidPriceLiveData = MutableLiveData<TransactionBid>()

    var truckGetLiveData = MutableLiveData<Pair<List<TruckResponseArray>,HomeBidsRequestItemData>>()

    var analyticsBucket :Boolean = false

    var indentLiveData = MutableLiveData<HashMap<Int, Triple<String,String?, String?>>>()
    var bidCount =0
    var lowestBid:Double?=0.0
    var restrictEventTrigger :Boolean=true
    var refreshCalled :Boolean=false

    companion object{
    var truckNumTextViewAdded :Boolean=false
    val indentMap = HashMap<Int, Triple<String, String?, String?>>()
   }

    lateinit var transaction: HomeBidsRequestItemData

    /* user bids live data */
    var userBidsData =
        MutableLiveData<List<Pair<BaseBulkBidSummaryRVAdapterItem<*>, DataRVAdapterOperationType>>>()

    var editBulkLiveData= MutableLiveData<Pair<Int,String>>()
    var editFlg= mutableListOf<Boolean>(false,false,false)


    /**
   * Fetch transaction details
   */
  fun fetchTransactionDetails() {
    compositeDisposable += transactionsRepository.transactionDetails(transactionId)
        .onBackground()
        .progress()
        .subscribe { _tRes, error ->
          if (!error) {
            transaction = _tRes
            transactionLiveData.postValue(_tRes)
            fetchTransactionBids()
          } else {
            transactionLiveData.postValue(null)
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


  /**
   * Fetch transaction bids and update UI as per response
   */
  fun fetchTransactionBids( action: Boolean = false) {
    compositeDisposable += bidsRepository.transactionBids(transactionId)
        .onBackground()
        .bidsProgress()
        .subscribe { _bRes, error ->
          if (!error) {
            bidCount=_bRes.third
            lowestBid=_bRes.first.second?.bidAmount
            //determine bid state and post to live data
            when {
              _bRes.third == 0 -> {
                transactionBidLiveData.postValue(
                    BidDetailsUserBidState_PlaceBidFirst()
                )
                bidPriceLiveData.postValue(null)
              }
              _bRes.first.first == null -> {
                transactionBidLiveData.postValue(
                    BidDetailsUserBidState_PlaceBid(
                        _bRes.third, _bRes.second, _bRes.first, transaction.isPMTIndent()
                    )
                )
                bidPriceLiveData.postValue(null)
              }
              else -> if(((transaction.isDMTIndent() || dmtStatus == "dmt" ) && !fromPage )|| (dmtStatus == "dmt" && active)){
                  transactionBidLiveData.postValue(
                          BidDetailsUserBidState_BulkLoad_Edit(
                                  _bRes.third, _bRes.second, _bRes.first, transaction.isPMTIndent()
                          )
                  )
                  bidPriceLiveData.postValue(null)
              }else{
                  when (_bRes.first.first!!.status()) {
                      Accepted -> {
                          bidPriceLiveData.postValue(_bRes.first.first)
                          fetchTripDetails()
                      }
                      Rejected -> {
                          try {
                              transactionBidLiveData.postValue(
                                      BidDetailsUserBidState_RejectedBid(
                                              _bRes.second.acceptedBid()!!, _bRes.first.first!!,
                                              transaction.isPMTIndent()
                                      )
                              )
                          } catch (e: Exception) {

                          } finally {
                              bidPriceLiveData.postValue(null)
                          }
                      }
                      Cancelled -> {
                          try {
                              transactionBidLiveData.postValue(
                                      BidDetailsUserBidState_CancelledBid(
                                              _bRes.first.first!!,
                                              transaction.isPMTIndent()
                                      )
                              )
                          } catch (e: Exception) {

                          } finally {
                              bidPriceLiveData.postValue(null)
                          }
                      }
                      else -> {
                          if(action){
                              analyticsBucket=true
                          }
                          transactionBidLiveData.postValue(
                                  BidDetailsUserBidState_EditBid(
                                          _bRes.third, _bRes.second, _bRes.first, transaction.isPMTIndent()
                                  )
                          )
                          bidPriceLiveData.postValue(null)
                      }
                  }
              }
            }
          } else {
            error.handle()
          }
        }
  }

  private fun fetchTripDetails() {
    compositeDisposable += tripsRepository.tripAndTransactionDetails(transactionId)
        .onBackground()
        .bidsProgress()
        .subscribe { _res, error ->
          if (!error) {
            transactionBidLiveData.postValue(
                BidDetailsUserBidState_ConfirmedBid(
                    _res.first.pickupLocation, _res.second.driverDetails,
                    _res.second.vehicleDetails.vehicleNo
                )
            )
          } else {
            error.handle()
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
        isPMT, transactionId, bidAmount, pmtRate, commercialType,
            expectedArrivalTimePickup, expectedArrivalTimePickupRemark
    )
        .delay(BidsUpdateDelay, SECONDS)
        .onBackground()
        .bidsProgress()
        .subscribe { _res, error ->
          if (!error && _res.isSuccess) {
              fetchTransactionBids(true)
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
        isPMT, transactionId, bidId, bidAmount, commercialType, pmtRate,
            expectedArrivalTimePickup, expectedArrivalTimePickupRemark
    )
        .delay(BidsUpdateDelay, SECONDS)
        .onBackground()
        .bidsProgress()
        .subscribe { _res, error ->
          if (!error && _res.isSuccess) {
              fetchTransactionBids(true)

          } else {
            error.handle()
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
          .onBackground()
          .bidsProgress()
          .subscribe { _res, error ->
              if (!error && _res!=null) {
                  fetchTransactionBids(true)

              } else {
                  error.handle()
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
              .bidsProgress()
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
              .bidsProgress()
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
              .bidsProgress()
              .subscribe{_res, error ->
                  if (!error && _res != null) {
                      editFlg[2]= true
                      editBulkLiveData.postValue(Pair(30, transactionId))
                  }
                  else{
                      error.handle()
                      editFlg[2]= true
                      editBulkLiveData.postValue(Pair(31, transactionId))

                  }
              }

      }
      else
      {
         editFlg[2]= true
      }

  }



    /**
   * filter accepted Bid
   */
  private fun List<TransactionBid>.acceptedBid() = firstOrNull { it._status == Accepted.statusKey }

  /**
   * Emit bids fetching progress
   */
  private fun <T> Single<T>.bidsProgress() = doOnSubscribe {
    if (transactionBidLiveData.value !is BidDetailsUserBidState_LoadingBids)
      transactionBidLiveData.postValue(BidDetailsUserBidState_LoadingBids())
  }

    fun getUserBulkBids(userBids:List<TransactionBid>? , lowestBid: Double?= 0.0) {
        val bulkBidSummaryItemDataList: ArrayList<BulkBidSummaryItemData> = ArrayList()
        val bulkBidSummaryItemList:ArrayList<Pair<BaseBulkBidSummaryRVAdapterItem<*>, DataRVAdapterOperationType>> = ArrayList()


        val bids = mutableListOf<TransactionBid>()
        //map same vehicle type with bids
        val map: MutableMap<String, MutableList<TransactionBid>?> = HashMap()
        if (userBids != null) {
            for (bid in userBids) {
                val key: String = bid.vehicleType!!
                if (map.containsKey(key)) {
                    val list: MutableList<TransactionBid>? = map[key]
                    list!!.add(bid)
                } else {
                    val list: MutableList<TransactionBid> = ArrayList<TransactionBid>()
                    list.add(bid)
                    map[key] = list
                }

            }
        }
        //get count of status
        for(key in map.keys){
            var openStat: String= ""
            var lostStat: String =""
            var confirmedStat: String= ""
            val truckCount:Int?=map[key]?.size
            var openStatus:Int=0
            var lostStatus:Int=0
            var confirmedStatus:Int=0
            val vehicleNumberLoc: MutableMap<String, String> = mutableMapOf<String, String>()


            var bidAmt = 0.0
            var confirmAmt =0.0
            var lostAmt= 0.0


            for(bid in map[key]!!){
                when (bid._status) {
                    "open" -> {
                        openStatus+=1
                        bidAmt = bid.bidAmount
                    }
                    "accepted" -> {
                        confirmedStatus+=1
                        confirmAmt = bid.bidAmount

                        vehicleNumberLoc.put(bid.vehicleNumber.toString(),bid.childTransactionId.toString())
                    }
                    "rejected" -> {
                        lostAmt= bid.bidAmount
                        lostStatus+=1
                    }
                    "cancelled" ->{
                        lostAmt= bid.bidAmount
                        lostStatus+=1
                    }
                }
            }


            if(bidAmt == 0.0 && confirmAmt!= 0.0){
                bidAmt= confirmAmt
            }
            else if(bidAmt==0.0 && lostAmt!= 0.0){
                bidAmt=lostAmt
            }

            if(openStatus>0){
                openStat=("$openStatus Open:")
            }
            if(lostStatus>0){
                lostStat=("$lostStatus Lost:")
            }
            if(confirmedStatus>0){
                confirmedStat=("$confirmedStatus Confirmed")
            }
            val lowestBidVis = if(lowestBid!=0.0 ) bidAmt<=lowestBid!! else false

            val bulkBidsItem = BulkBidSummaryItemData(key, bidAmt ,truckCount!!, openStat,lowestBidVis,false, confirmedStat ,
                    lostStat , vehicleNumberLoc, map[key]!![0].childTransactionId,"BidDetail")
            bulkBidSummaryItemDataList.add(bulkBidsItem)
            bulkBidSummaryItemList.add(Pair(BulkBidSummaryItem(bulkBidsItem), DataRVAdapterOperationType.Add))
        }
            userBidsData.postValue(bulkBidSummaryItemList)

    }


    fun fetchIndentCenters(code:String, seq:Int) {
        indentMap.clear()
        compositeDisposable += warehouseRepository.getWarehouseDetails(WarehouseRequest("facility_code",code, "faas"))
                .onBackground()
                .subscribe { _tRes, error ->
                    if (!error) {
                        indentMap.put(seq, Triple(_tRes.city, _tRes.address,_tRes.pincode))
                        indentLiveData.postValue(indentMap)
                      }  else {
                        error.handle()
                    }
                }
    }

}

private const val BidsUpdateDelay = 1L // Delay in fetching bids after creating/updating