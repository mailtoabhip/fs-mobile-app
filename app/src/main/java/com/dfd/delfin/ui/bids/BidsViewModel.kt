package com.dfd.delfin.ui.bids

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.LoadCycleRepository
import com.dfd.delfin.api.repository.TransactionsRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.data.biddetail.BulkBidSummaryItemData
import com.dfd.delfin.data.bids.TransactionBid
import com.dfd.delfin.data.bids.TransactionBidStatus
import com.dfd.delfin.data.home.bids.HomeBidsRequestItemData
import com.dfd.delfin.database.AppDatabase
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.Add
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.dfd.delfin.ui.biddetails.*
import com.dfd.delfin.ui.bids.BidType.Unknown
import com.dfd.delfin.ui.home.fragments.bids.BaseHomeBidsRVAdapterItem
import com.dfd.delfin.ui.home.fragments.bids.HomeBidsProgressItem
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class BidsViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val userRepository: UserRepository,
  private val loadCycleRepository: LoadCycleRepository,
  private val appDatabase: AppDatabase
) : BaseViewModel(), BulkBidDetailsDialog.BulkBidDetailsDialogInterface {

  /* Bids live data */
  var bidsLiveData =
    MutableLiveData<List<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /*bids count live data*/
  var bidsCountLiveData = MutableLiveData<Int>()

  /* data loading live data*/
  var dataLoadingLiveData = MutableLiveData<Boolean>()

  /* bid type */
  var bidType: BidType = Unknown

  /* pagination params */
  var total = 0
  var offset = 0
  var hasMoreData = true
  var bidSuggestion = false

  /* transaction id */
  lateinit var transactionId: String
  /**
   * Fetch bids
   */
  fun fetchBids(paginate: Boolean) {
    if (bidType == Unknown) return

    if (!paginate) {
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }

    if (paginate) {
      showProgress()
      /* add progress if not paginating */
      Pair(HomeBidsProgressItem(), AddUpdate).let { bidsLiveData.postValue(listOf(it)) }
    }

    dataLoadingLiveData.postValue(true)
    var contract:Boolean? = null
    var onlyFrcBids:Boolean?=null
    var statuses:String? = bidType.status.statusKey
    var pending:Boolean? = false
    if (bidType == BidType.LostBid) {
      statuses = mutableListOf<String>().apply {
        add(BidType.LostBid.status.statusKey)
        add(TransactionBidStatus.Cancelled.statusKey)
      }
        .joinToString(separator = ",") { it }
    }

    if (bidType == BidType.ConfirmedBid) {
      pending= true
    }
    if(bidType==BidType.ContractBid){
      statuses = null
      contract = true
      onlyFrcBids = true
      pending = null
    }
    val mainTrace = Firebase.performance.newTrace("fetch_bids_by_type")
    val parallelTrace = Firebase.performance.newTrace("fetch_bids_by_type_and_lowest_bids_on_txns_parallel")
    mainTrace.start()
  }

  /**
   * Fetch transaction bids and update UI as per response
   */
  var transactionBidLiveData = MutableLiveData<BidDetailsUserBidState>()
  lateinit var transaction: HomeBidsRequestItemData


  override fun getUserBulkBidsAgainstTrans(userBids: List<TransactionBid>?): ArrayList<Pair<BaseBulkBidSummaryRVAdapterItem<*>, DataRVAdapterOperationType>>? {
    val bulkBidSummaryItemDataList: ArrayList<BulkBidSummaryItemData> = ArrayList()
    val bulkBidSummaryItemList:ArrayList<Pair<BaseBulkBidSummaryRVAdapterItem<*>, DataRVAdapterOperationType>> = ArrayList()

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
      var lostStat: String=""
      var confirmedStat: String=""
      val truckCount:Int?=map[key]?.size
      var openStatus:Int=0
      var lostStatus:Int=0
      var confirmedStatus:Int=0

      val vehicleNumberLoc: MutableMap<String, String> = mutableMapOf<String, String>()

      var bidAmt =0.0
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

      val bulkBidsItem = BulkBidSummaryItemData(key, bidAmt, truckCount!!, openStat , lowestBidStatus = false, expanded = false,
              confirmedStatus = confirmedStat, lostStatus = lostStat, vehicleNumber = vehicleNumberLoc, childTransactionId = map[key]!![0].childTransactionId)
      bulkBidSummaryItemDataList.add(bulkBidsItem)

      bulkBidSummaryItemList.add(Pair(BulkBidSummaryItem(bulkBidsItem), Add))
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