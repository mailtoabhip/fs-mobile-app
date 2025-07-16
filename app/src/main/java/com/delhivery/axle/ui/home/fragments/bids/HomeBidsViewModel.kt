package com.delhivery.axle.ui.home.fragments.bids

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.LoadCycleRepository
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.response.FrequentTripsResponse
import com.delhivery.axle.api.response.LowestBidResponse
import com.delhivery.axle.api.response.TransactionsResponse
import com.delhivery.axle.data.Quintuple
import com.delhivery.axle.data.biddetail.BulkBidSummaryItemData
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsHeaderItemData
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.database.AppDatabase
import com.delhivery.axle.database.entity.OffersEntity
import com.delhivery.axle.exception.NoBidsFoundException
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Update
import com.delhivery.axle.ui.biddetails.BaseBulkBidSummaryRVAdapterItem
import com.delhivery.axle.ui.biddetails.BulkBidSummaryItem
import com.delhivery.axle.ui.bids.BidType
import com.delhivery.axle.ui.bids.BulkBidDetailsDialog
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.Single
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * View model class for [HomeBidsFragment]
 *
 **
 */
class HomeBidsViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val bidsRepository: BidsRepository,
  private val loadCycleRepository: LoadCycleRepository,
  private val userRepository: UserRepository,
  private val appDatabase: AppDatabase
) : BaseViewModel(),BulkBidDetailsDialog.BulkBidDetailsDialogInterface {

  /* user bids live data */
  var userBidsData =
    MutableLiveData<List<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* bids count live data */
  var bidsCountLiveData = MutableLiveData<Int>()

  /* data loading live data */
  var dataLoadingLiveData = MutableLiveData<Boolean>()

  /* pagination params */
  var total = 0
  var offset = 0
  var hasMoreData = true
  var activeBids = ""
  var confirmedBids= ""
  var lostBids= ""
  var contractBids= ""

  //
  var bidType : BidType = BidType.ActiveBid
  /**
   * Fetch bids summary
   */
  fun fetchBidsSummary(bidType: BidType) {
    compositeDisposable += bidsRepository.userBidsSummary()
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            activeBids=_res.myBids.toString()
            confirmedBids=_res.confirmedBids.toString()
            lostBids=_res.lostBids.toString()
            contractBids = _res.contractBids.toString()
            mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(
                  Pair(
                      HomeBidsHeaderItem(
                          HomeBidsHeaderItemData(
                              _res.myBids,
                              _res.confirmedBids,
                              _res.lostBids,
                            _res.contractBids,
                            bidType
                          )
                      ), Update
                  )
              )
            }
                .let { userBidsData.postValue(it) }
          } else {
            error.handle()
          }
        }
  }

  /**
   * Fetch bids
   *
   *
   */
  fun fetchBids(bidType: BidType,paginate: Boolean = false) {

    if (!paginate) {
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }

    /* add progress if not paginating */
    if (paginate) {
      Pair(HomeBidsProgressItem(), AddUpdate).let { userBidsData.postValue(listOf(it)) }
    }

    //Send ONE AT A TIME
    val statuses = mutableListOf<String>().apply {
      add(bidType.status.statusKey)
//      add(BidType.ActiveBid.status.statusKey)
//      add(BidType.ConfirmedBid.status.statusKey)
//      add(BidType.LostBid.status.statusKey)
    }
        .joinToString(separator = ",") { it }

    dataLoadingLiveData.postValue(true)
    val mainTrace = Firebase.performance.newTrace("fetch_bids_placed_by_supplier")
    val parallelTrace = Firebase.performance.newTrace("fetch_bids_placed_and_lowest_bids_on_txns_parallel")
    mainTrace.start()
    //Fetching all user bids from server
    //sending the contract param as null will include "contract" type bids into the response
    compositeDisposable += bidsRepository.userBids(offset, statuses, true,null,null)
      .flatMap { _res ->
        total = _res.first
        offset = _res.third
        hasMoreData = _res.fourth
        bidsCountLiveData.postValue(total)
        if (!paginate && _res.first == 0) {
          Single.error(NoBidsFoundException())
        } else {
          parallelTrace.start()
          Single.zip(
            //fetch all transaction data
            //request = send all the transactionIds
            //response = [{transoBJ}, {transoBJ2}]
            transactionsRepository.bulkTransactions(_res.second).subscribeOn(Schedulers.io()),
            //to fetch the lowest bid from all the transactions ids
            //response = map [tid, lowest_price]
            bidsRepository.bulkLowestBidsForTransactions(_res.second).subscribeOn(Schedulers.io()),
            //fetch all the bids data against all the transactionIds
            bidsRepository.bidsForBulkLoads(_res.second).subscribeOn(Schedulers.io()),
            Function3<Pair<List<TransactionBid>, TransactionsResponse>, List<LowestBidResponse>,Pair<List<TransactionBid>, List<TransactionBid>>,
                    Quintuple<List<TransactionBid>, TransactionsResponse, List<LowestBidResponse>, List<TransactionBid>,List<TransactionBid>>> { t1, t2,t3 ->
              Quintuple(t1.first, t1.second, t2,t3.first,t3.second)
            })
        }
      }
        .onBackground()
        .subscribe { _res, error ->
          if(error != null) mainTrace.putAttribute("error_response_received",error.message.toString())
          parallelTrace.stop()
          mainTrace.stop()
          if (!error) {

            mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeBidsProgressItem(), Remove))

              /* edit route prefs, if fresh fetch n total == 0 */
              if (!paginate && total == 0) {
                add(Pair(HomeBidsWarningItem_NoBids, AddUpdate))
              }
              /* post all transactions mapped to bids as add */
              else {
                add(Pair(HomeBidsSearchItem(), AddUpdate))

                val bids = _res.first
                val transactions = _res.second.transactions
                val map: MutableMap<String, MutableList<TransactionBid>?> = HashMap()
                for (bid in _res.fifth) {
                  val key: String = bid.transactionId
                  if (map.containsKey(key)) {
                    val list: MutableList<TransactionBid>? = map[key]
                    list!!.add(bid)
                  } else {
                    val list: MutableList<TransactionBid> = ArrayList<TransactionBid>()
                    list.add(bid)
                    map[key] = list
                  }
                }
                for (transaction in transactions) {
                  try {
                    val lowestBid = _res.third.filter { b ->
                      b.transactionId.safeEquals(transaction.transactionId)
                    }[0]
                    transaction.numBids = lowestBid.numBids
                    transaction.lowestBid = lowestBid.minBid
                    transaction.loadPricePercent = _res.second.loadPricePercent
                    //set bidType as well
                    transaction.bidType = bidType
                    transaction.transactionBid = bids.filter { b ->
                      b.transactionId.safeEquals(transaction.transactionId)
                    }[0]
                    transaction.bulkTransactionBids = map[transaction.transactionId]!!
                  } catch (e: Exception) {
                    transaction.transactionId?.let { Log.d("No Bid found for: ", it) }
                  }
                  add(Pair(HomeBidsRequestItem(transaction), Add))
                }
              }
            }
                .let { userBidsData.postValue(it) }
          } else {
            mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeBidsProgressItem(), Remove))
              /* remove search item */
              add(Pair(HomeBidsSearchItem(), Remove))
              if (error is NoBidsFoundException) {
                /* add no bids warning item */
                add(Pair(HomeBidsWarningItem_NoBids, AddUpdate))
              } else {
                /* add api time out item */
                add(Pair(HomeBidsWarningItem_TimeOut, AddUpdate))
              }
            }
                .let { userBidsData.postValue(it) }
          }

          dataLoadingLiveData.postValue(false)
        }
  }

  override fun getUserBulkBidsAgainstTrans(userBids: List<TransactionBid>?): ArrayList<Pair<BaseBulkBidSummaryRVAdapterItem<*>, DataRVAdapterOperationType>>? {
    val bulkBidSummaryItemDataList: ArrayList<BulkBidSummaryItemData> = ArrayList()
    val bulkBidSummaryItemList:ArrayList<Pair<BaseBulkBidSummaryRVAdapterItem<*>, DataRVAdapterOperationType>> = ArrayList()


    //map same vehicle type with bids
    val map: MutableMap<String, MutableList<TransactionBid>?> = HashMap()
    for (bid in userBids!!) {
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

    //get count of status
    for(key in map.keys){
      var openStat: String=""
      var lostStat: String=""
      var confirmedStat: String=""
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
            confirmAmt= bid.bidAmount
            vehicleNumberLoc.put(bid.vehicleNumber.toString(),bid.childTransactionId.toString())
          }
          "rejected" -> {
            lostAmt= bid.bidAmount
            lostStatus+=1
          }
          "cancelled" ->{
            lostAmt=bid.bidAmount
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
      val bulkBidsItem = BulkBidSummaryItemData(key, bidAmt, truckCount!!,openStat!!, lowestBidStatus = false, expanded = false, confirmedStatus = confirmedStat,
              lostStatus = lostStat, vehicleNumber = vehicleNumberLoc, childTransactionId = map[key]!![0].childTransactionId)
      bulkBidSummaryItemDataList.add(bulkBidsItem)
      bulkBidSummaryItemList.add(Pair(BulkBidSummaryItem(bulkBidsItem), DataRVAdapterOperationType.Add))
    }
    return bulkBidSummaryItemList
  }

  var offerLiveData = MutableLiveData<HomeBidsRequestItemData?>()

  fun fetchDatabaseOffers(data: HomeBidsRequestItemData?){
    val lrt = appDatabase.offersDao().getParticularsOffers(data?.originCityCode, data?.destinationCityCode)
    if(!lrt.isNullOrEmpty() && lrt.size>0){
      data?.resOffer = Triple(Pair(true, lrt[0].offerId), Pair(data?.truckSpecification?.truckDispName, lrt.get(0).tdn), Triple(lrt[0].occ, lrt.get(0).dcc,lrt.get(0).amt.toString()))
    }else{
      data?.resOffer = Triple(Pair(false, null),Pair(null, null), Triple(null,null,null))
    }
    offerLiveData.postValue(data)
  }

}