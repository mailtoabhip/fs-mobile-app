package com.delhivery.axle.ui.home.fragments.bids

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.LoadCycleRepository
import com.delhivery.axle.api.repository.SpotBiddingRepository
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.response.FrequentTripsResponse
import com.delhivery.axle.api.response.InitiateCallResponse
import com.delhivery.axle.api.response.LowestBidResponse
import com.delhivery.axle.api.response.TransactionsResponse
import com.delhivery.axle.data.Quintuple
import com.delhivery.axle.data.biddetail.BulkBidSummaryItemData
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.TransactionBidStatus
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
  private val spotBiddingRepository: SpotBiddingRepository,
  private val appDatabase: AppDatabase
) : BaseViewModel(),BulkBidDetailsDialog.BulkBidDetailsDialogInterface {

  /* user bids live data */
  var userBidsData =
    MutableLiveData<List<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* bids count live data */
  var bidsCountLiveData = MutableLiveData<Int>()

  /* data loading live data */
  var dataLoadingLiveData = MutableLiveData<Boolean>()

  /* marketplace call initiation live data */
  var callInitiationLiveData = MutableLiveData<InitiateCallResponse>()
  
  /* marketplace call error live data */
  var callInitiationErrorLiveData = MutableLiveData<String>()
  
  /* marketplace call loading state - maps transactionId to loading state */
  var callLoadingStateLiveData = MutableLiveData<Map<String, Boolean>>()

  /* pagination params */
  var total = 0
  var offset = 0
  var hasMoreData = true
  var activeBids = ""
  var confirmedBids= ""
  var lostBids= ""
  var contractBids= ""
  var bidSuggestion = false

  //
  var bidType : BidType = BidType.ActiveBid
  
  // Store user data for payment fields
  var user: com.delhivery.axle.data.UserModel? = null
  
  /**
   * Helper method to populate payment fields from user data
   */
  private fun populatePaymentFields(transaction: HomeBidsRequestItemData) {
    user?.supplierDetails?.let { supplier ->
      transaction.paymentMode = supplier.paymentMode
      transaction.advancePercentage = supplier.advancePercentage
    }
  }
  
  /**
   * Initiate marketplace call to get bridge number
   *
   * @param transactionId Transaction ID from marketplace load
   * @param bidId Bid ID for the transaction
   */
  fun initiateMarketplaceCall(transactionId: String, bidId: String) {
    Log.d("HomeBidsViewModel", "==================== INITIATING CALL ====================")
    Log.d("HomeBidsViewModel", "Transaction ID: $transactionId")
    Log.d("HomeBidsViewModel", "Bid ID: $bidId")
    Log.d("HomeBidsViewModel", "Source: marketplace")
    Log.d("HomeBidsViewModel", "========================================================")
    
    // Set loading state to true for this transaction
    updateCallLoadingState(transactionId, true)
    
    compositeDisposable += spotBiddingRepository.initiateMarketplaceCall(
      transactionId = transactionId,
      bidId = bidId,
      source = "marketplace"
    )
      .onBackground()
      .subscribe({ response ->
        Log.d("HomeBidsViewModel", "==================== CALL INITIATION SUCCESS ====================")
        Log.d("HomeBidsViewModel", "Response Success: ${response.success}")
        Log.d("HomeBidsViewModel", "Bridge Numbers Count: ${response.data?.size ?: 0}")
        response.data?.forEachIndexed { index, bridgeData ->
          Log.d("HomeBidsViewModel", "Bridge #${index + 1}:")
          Log.d("HomeBidsViewModel", "  - Number: ${bridgeData.bridgeNumber}")
          Log.d("HomeBidsViewModel", "  - Vendor: ${bridgeData.vendor}")
          Log.d("HomeBidsViewModel", "  - Expiry: ${bridgeData.expiry}")
        }
        Log.d("HomeBidsViewModel", "================================================================")
        
        // Set loading state to false for this transaction
        updateCallLoadingState(transactionId, false)
        
        if (response.success && !response.data.isNullOrEmpty()) {
          callInitiationLiveData.postValue(response)
        } else {
          Log.w("HomeBidsViewModel", "Response success was false or data was empty")
          callInitiationErrorLiveData.postValue("Unable to initiate call. Please try again.")
        }
      }, { error ->
        // Log complete error details for debugging
        Log.e("HomeBidsViewModel", "==================== CALL INITIATION ERROR ====================")
        Log.e("HomeBidsViewModel", "Error Type: ${error.javaClass.name}")
        Log.e("HomeBidsViewModel", "Error Message: ${error.message}")
        Log.e("HomeBidsViewModel", "Error Cause: ${error.cause?.message}")
        Log.e("HomeBidsViewModel", "Error Stack Trace:", error)
        
        // Log request details
        Log.e("HomeBidsViewModel", "Request Details:")
        Log.e("HomeBidsViewModel", "  - Transaction ID: $transactionId")
        Log.e("HomeBidsViewModel", "  - Bid ID: $bidId")
        Log.e("HomeBidsViewModel", "  - Source: marketplace")
        Log.e("HomeBidsViewModel", "===============================================================")
        
        // Provide more specific error messages based on error type
        val errorMessage = when {
          error is java.net.UnknownHostException -> {
            Log.e("HomeBidsViewModel", "DNS Error: Unable to resolve hostname - ${error.message}")
            "Network error: Unable to reach server. Please check your VPN connection and internet connectivity."
          }
          error is java.net.SocketTimeoutException -> {
            Log.e("HomeBidsViewModel", "Timeout Error: ${error.message}")
            "Request timed out. Please check your internet connection and try again."
          }
          error is java.net.ConnectException -> {
            Log.e("HomeBidsViewModel", "Connection Error: ${error.message}")
            "Unable to connect to server. Please check your network connection."
          }
          error is java.io.IOException -> {
            Log.e("HomeBidsViewModel", "IO Error: ${error.message}")
            "Network error: ${error.message ?: "Unable to connect to server"}"
          }
          error is retrofit2.HttpException -> {
            Log.e("HomeBidsViewModel", "HTTP Error: Code=${error.code()}, Message=${error.message()}")
            "Server error: ${error.message()}"
          }
          else -> {
            Log.e("HomeBidsViewModel", "Unknown Error: ${error.javaClass.name} - ${error.message}")
            error.message ?: "Failed to initiate call. Please check your connection."
          }
        }
        
        // Set loading state to false for this transaction
        updateCallLoadingState(transactionId, false)
        
        callInitiationErrorLiveData.postValue(errorMessage)
      })
  }
  
  /**
   * Update call loading state for a specific transaction
   */
  private fun updateCallLoadingState(transactionId: String, isLoading: Boolean) {
    val currentMap = callLoadingStateLiveData.value?.toMutableMap() ?: mutableMapOf()
    currentMap[transactionId] = isLoading
    callLoadingStateLiveData.postValue(currentMap)
  }
  
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
      when (bidType) {
        BidType.ActiveBid -> {
          // Ongoing tab - fetch only "open" status bids
          add(TransactionBidStatus.Open.statusKey)
          bidSuggestion = true
        }
        BidType.ConfirmedBid -> {
          // Closed tab - fetch rejected, cancelled, and accepted status bids
          add(TransactionBidStatus.Rejected.statusKey)   // "rejected"
          add(TransactionBidStatus.Cancelled.statusKey)  // "cancelled"
          add(TransactionBidStatus.Accepted.statusKey)   // "accepted"
        }
        BidType.LostBid -> {
          // Lost bids - fetch rejected and cancelled
          add(BidType.LostBid.status.statusKey)
          add(TransactionBidStatus.Cancelled.statusKey)
        }
        else -> {
          // Default - use the status from bidType
          add(bidType.status.statusKey)
        }
      }
    }
      .joinToString(separator = ",") { it }

    dataLoadingLiveData.postValue(true)
    
    // Fetch user data first if not available
    if (user == null) {
      compositeDisposable += userRepository.getUser(false)
        .onBackground()
        .subscribe { userModel, error ->
          if (!error && userModel != null) {
            this.user = userModel
            // Now proceed with fetching bids
            fetchBidsData(bidType, paginate, statuses)
          } else {
            error?.handle()
            dataLoadingLiveData.postValue(false)
          }
        }
    } else {
      // User data already available, proceed with bids
      fetchBidsData(bidType, paginate, statuses)
    }
  }
  
  /**
   * Fetch bids data (separated from user data fetching)
   */
  private fun fetchBidsData(bidType: BidType, paginate: Boolean, statuses: String) {
    val mainTrace = Firebase.performance.newTrace("fetch_bids_placed_by_supplier")
    val parallelTrace = Firebase.performance.newTrace("fetch_bids_placed_and_lowest_bids_on_txns_parallel")
    mainTrace.start()
    //Fetching all user bids from server
    //sending the contract param as null will include "contract" type bids into the response
    compositeDisposable += bidsRepository.userBids(offset, statuses, true,null,null,bidSuggestion)
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
            Log.d("bidsview","$paginate $total")
            /* edit route prefs, if fresh fetch n total == 0 */
            if (!paginate && total == 0) {
              add(Pair(HomeBidsWarningItem_NoBids, AddUpdate))
            }
            /* post all transactions mapped to bids as add */
            else {
              add(Pair(HomeBidsSearchItem(), AddUpdate))

              val bids = _res.first
              val transactions = _res.second.transactions ?: emptyList()
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
                  
                  // Sync bid suggestion values from transactionBid to transaction fields
                  transaction.transactionBid?.suggestion?.let { suggestion ->
                    transaction.bidSuggestion = true
                    transaction.suggestedBidMessage = suggestion.message
                  } ?: run {
                    transaction.bidSuggestion = false
                    transaction.suggestedBidMessage = null
                  }
                  
                  transaction.bulkTransactionBids = map[transaction.transactionId]!!
                } catch (e: Exception) {
                  transaction.transactionId?.let { Log.d("No Bid found for: ", it) }
                }
                // Populate payment fields from user data
                populatePaymentFields(transaction)
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
            if (error is NoBidsFoundException &&!paginate) {
              /* add no bids warning item */
              add(Pair(HomeBidsWarningItem_NoBids, AddUpdate))
            } else if(!paginate){
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
      val key: String = bid.vehicleType?:""
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