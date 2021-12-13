package com.delhivery.axle.ui.bids

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.response.LowestBidResponse
import com.delhivery.axle.api.response.TransactionsResponse
import com.delhivery.axle.data.Quintuple
import com.delhivery.axle.data.biddetail.BulkBidSummaryItemData
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.TransactionBidStatus
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.exception.NoBidsFoundException
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.biddetails.*
import com.delhivery.axle.ui.bids.BidType.Unknown
import com.delhivery.axle.ui.home.fragments.bids.BaseHomeBidsRVAdapterItem
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsProgressItem
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRequestItem
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsWarningItem_NoBids
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsWarningItem_TimeOut
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import io.reactivex.Single
import io.reactivex.functions.Function3
import javax.inject.Inject

/**
 * View model for [BidsActivity]
 */
class BidsViewModel @Inject constructor(
  private val bidsRepository: BidsRepository,
  private val transactionsRepository: TransactionsRepository
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

  /* transaction id */
  lateinit var transactionId: String
  lateinit var requestType: String
  /**
   * Fetch bids
   */
  fun fetchBids(paginate: Boolean) {
    if (bidType == Unknown) return

    if (!paginate) {
      offset = 0
    } else if (paginate && (total == offset)) {
      return
    }

    if (paginate) {
      showProgress()
      /* add progress if not paginating */
      Pair(HomeBidsProgressItem(), AddUpdate).let { bidsLiveData.postValue(listOf(it)) }
    }

    dataLoadingLiveData.postValue(true)

    var statuses = bidType.status.statusKey
    if (bidType == BidType.LostBid) {
       statuses = mutableListOf<String>().apply {
        add(BidType.LostBid.status.statusKey)
        add(TransactionBidStatus.Cancelled.statusKey)
      }
        .joinToString(separator = ",") { it }
    }

    compositeDisposable += bidsRepository.userBids(offset, statuses)
        .flatMap { _res ->
          total = _res.first
          bidsCountLiveData.postValue(total)
          if (!paginate && _res.first == 0) {
            Single.error(NoBidsFoundException())
          } else {
            Single.zip(
                transactionsRepository.bulkTransactions(_res.second),
                bidsRepository.bulkLowestBidsForTransactions(_res.second),
                bidsRepository.bidsForBulkLoads(_res.second),
                Function3<Pair<List<TransactionBid>, TransactionsResponse>, List<LowestBidResponse>,Pair<List<TransactionBid>, List<TransactionBid>>,
                        Quintuple<List<TransactionBid>, TransactionsResponse, List<LowestBidResponse>, List<TransactionBid>,List<TransactionBid>>> { t1, t2,t3 ->
                  Quintuple(t1.first, t1.second, t2,t3.first,t3.second)
                })
          }
        }
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error) {
            offset += _res.second.offset
            hasMoreData = _res.second.hasNext

            mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeBidsProgressItem(), Remove))

              if (!paginate && total == 0) {
                add(Pair(HomeBidsWarningItem_NoBids, AddUpdate))
                /* post all transactions mapped to bids as add */
              } else {
                val bids = _res.first
                val transactions = _res.second.transactions
                val map: MutableMap<String, MutableList<TransactionBid>?> = HashMap()
                for (bid in _res.fifth) {
                  val key: String = bid.transactionId!!
                  if (map.containsKey(key)) {
                    val list: MutableList<TransactionBid>? = map[key]
                    list!!.add(bid)
                  } else {
                    val list: MutableList<TransactionBid> = ArrayList<TransactionBid>()
                    list.add(bid)
                    map[key] = list
                  }
                }
                var index = 0
                for (transaction in transactions) {
                  try {
                    val lowestBid = _res.third.filter { b ->
                      b.transactionId.safeEquals(
                          transaction.transactionId
                      )
                    }[0]
                   // transaction.bulkTransactionBids = _res.fourth
                    transaction.numBids = lowestBid.numBids
                    transaction.lowestBid = lowestBid.minBid
                    transaction.loadPricePercent = _res.second.loadPricePercent
                    index++
                    transaction.transactionBid = bids.filter { b ->
                      b.transactionId.safeEquals(transaction.transactionId)
                    }[0]
                    transaction.bulkTransactionBids= map.get(transaction.transactionId)
                  } catch (e: Exception) {
                    transaction.transactionId?.let { Log.d("No Bid found for: ", it) }
                  }
                  add(Pair(HomeBidsRequestItem(transaction), Add))
                }
              }
            }
                .let {
                  bidsLiveData.postValue(it)
                }
          } else {
            mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeBidsProgressItem(), Remove))
              if (error is NoBidsFoundException) {
                /* add no bids warning item */
                add(Pair(HomeBidsWarningItem_NoBids, AddUpdate))
              } else {
                /* add api time out item */
                add(Pair(HomeBidsWarningItem_TimeOut, AddUpdate))
              }
            }
                .let { bidsLiveData.postValue(it) }
          }

          dataLoadingLiveData.postValue(false)
        }
  }

  /**
   * Fetch transaction bids and update UI as per response
   */
  var transactionBidLiveData = MutableLiveData<BidDetailsUserBidState>()
  lateinit var transaction: HomeBidsRequestItemData

   fun fetchTransactionBids() {
    compositeDisposable += bidsRepository.transactionBids(transactionId)
      .onBackground()
      .progress()
      .subscribe { _bRes, error ->
        if (!error) {
          //determine bid state and post to live data
        //  if (requestType == "dmt") {
            transactionBidLiveData.postValue(
              BidDetailsUserBidState_BulkLoad_Edit(
                _bRes.third, _bRes.second, _bRes.first,true
              )
            )

        //  }
        }
        else {
          error.handle()
        }
      }
  }



  override fun getUserBulkBidsAgainstTrans(userBids: List<TransactionBid>?): ArrayList<Pair<BaseBulkBidSummaryRVAdapterItem<*>, DataRVAdapterOperationType>>? {
    val bulkBidSummaryItemDataList: ArrayList<BulkBidSummaryItemData>? = ArrayList()
    val bulkBidSummaryItemList:ArrayList<Pair<BaseBulkBidSummaryRVAdapterItem<*>, DataRVAdapterOperationType>>? = ArrayList()
    //Test data
    val bids: ArrayList<TransactionBid>?=ArrayList()
   // bids?.add()
    bids?.add(TransactionBid("","open",false,"","","","",6000.0,12000.0,"1","","","","","6_TYRE","KA08C5678"))
    bids?.add(TransactionBid("","confirmed",false,"","","","",6000.0,4444.0,"2","","","","","6_TYRE","KA08C5678"))
    bids?.add(TransactionBid("","confirmed",false,"","","","",6000.0,12000.0,"3","","","","","6_TYRE","KA08C5678"))
    bids?.add(TransactionBid("","open",false,"","","","",6000.0,12000.0,"4","","","","","7_TYRE","KA08C5678"))
    bids?.add(TransactionBid("","rejected",false,"","","","",6000.0,5555.0,"5","","","","","7_TYRE","KA08C5678"))

    //map same vehicle type with bids
    val map: MutableMap<String, MutableList<TransactionBid>?> = HashMap()
    for (bid in bids!!) {
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
      var openStat: String?=null
      var lostStat: String?=null
      var confirmedStat: String?=null
      val truckCount:Int?=map[key]?.size
      var openStatus:Int=0
      var lostStatus:Int=0
      var confirmedStatus:Int=0
    //  var vehicleNumberloc: Array<String?>?=null
      val vehicleNumberLoc: MutableList<String> = ArrayList()

      for(bid in map[key]!!){
        when (bid._status) {
          "open" -> {
            openStatus+=1
          }
          "confirmed" -> {
            confirmedStatus+=1
            vehicleNumberLoc.add(bid.vehicleNumber.toString())
            System.out.println("vehicle"+bid.vehicleNumber)
          }
          "rejected" -> {
            lostStatus+=1
          }
        }
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
      val bulkBidsItem = BulkBidSummaryItemData(key,map[key]!!.get(0).pmtRate!!,truckCount!!,openStat!!,false,confirmedStat,lostStat,vehicleNumberLoc,map[key]!!.get(0).childTransactionId)
      bulkBidSummaryItemDataList?.add(bulkBidsItem)
      bulkBidSummaryItemList?.add(Pair(BulkBidSummaryItem(bulkBidsItem), DataRVAdapterOperationType.Add))
    }
      return bulkBidSummaryItemList
  }


}