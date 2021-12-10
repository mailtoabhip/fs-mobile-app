package com.delhivery.axle.ui.biddetails

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.repository.TripsRepository
import com.delhivery.axle.api.repository.TruckRepository
import com.delhivery.axle.data.biddetail.BulkBidSummaryItemData
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Cancelled
import com.delhivery.axle.data.bids.TransactionBidStatus.Rejected
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
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
  val userPrefs: UserPrefs
) : BaseViewModel(), BidDetailsCreateEditDialogInterface{

  /* transaction id */
  lateinit var transactionId: String
    lateinit var requestType: String
  /* live data */
  var transactionLiveData = MutableLiveData<HomeBidsRequestItemData>()

  var transactionBidLiveData = MutableLiveData<BidDetailsUserBidState>()

  var bidPriceLiveData = MutableLiveData<TransactionBid>()

    var analyticsBucket :Boolean = false

    lateinit var transaction: HomeBidsRequestItemData

    /* user bids live data */
    var userBidsData =
        MutableLiveData<List<Pair<BaseBulkBidSummaryRVAdapterItem<*>, DataRVAdapterOperationType>>>()

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
    fun fetchTruckType() {
        compositeDisposable += truckRepository.getTruckType()
            .onBackground()
            .subscribe { _tRes, error ->
                System.out.println("truckType"+_tRes +error)
            }
    }


  /**
   * Fetch transaction bids and update UI as per response
   */
  private fun fetchTransactionBids( action: Boolean = false) {
    compositeDisposable += bidsRepository.transactionBids(transactionId)
        .onBackground()
        .bidsProgress()
        .subscribe { _bRes, error ->
          if (!error) {
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
              else -> when (_bRes.first.first!!.status()) {
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
                    if(requestType=="dmt"){
                        transactionBidLiveData.postValue(
                            BidDetailsUserBidState_BulkLoad_Edit(
                                _bRes.third, _bRes.second, _bRes.first, transaction.isPMTIndent()
                            )
                        )
                        bidPriceLiveData.postValue(null)
                    }else{
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
    position: Int
  ) {
    compositeDisposable += bidsRepository.createBid(
        isPMT, transactionId, bidAmount, pmtRate, commercialType
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
    position: Int
  ) {
    compositeDisposable += bidsRepository.editBid(
        isPMT, transactionId, bidId, bidAmount, commercialType, pmtRate
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

    fun getUserBulkBids(userBids:List<TransactionBid>?) {
        val bulkBidSummaryItemDataList: ArrayList<BulkBidSummaryItemData>? = ArrayList()
        val bulkBidSummaryItemList:ArrayList<Pair<BaseBulkBidSummaryRVAdapterItem<*>, DataRVAdapterOperationType>>? = ArrayList()
        //Test data
        val bids: ArrayList<TransactionBid>?=ArrayList()
        bids?.add(TransactionBid("","open",false,"","","","",6000.0,12000.0,"1","","","","","6_TYRE","KA08C5678"))
        bids?.add(TransactionBid("","confirmed",false,"","","","",6000.0,4444.0,"2","","","","","6_TYRE","KA08C5678"))
        bids?.add(TransactionBid("","open",false,"","","","",6000.0,12000.0,"3","","","","","6_TYRE","KA08C5678"))
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
//            var vehicleNumberLoc: Array<String?>?=null
            val vehicleNumberLoc: MutableList<String> = ArrayList()

            for(bid in map[key]!!){
                when (bid._status) {
                    "open" -> {
                        openStatus+=1
                    }
                    "confirmed" -> {
                        confirmedStatus+=1
                        vehicleNumberLoc.add(bid.vehicleNumber.toString())
                        System.out.println("Arv"+ vehicleNumberLoc[0])
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
            val bulkBidsItem = BulkBidSummaryItemData(key,map[key]!!.get(0).pmtRate!!,truckCount!!,openStat!!,false,confirmedStat,lostStat,vehicleNumberLoc)
            bulkBidSummaryItemDataList?.add(bulkBidsItem)
            bulkBidSummaryItemList?.add(Pair(BulkBidSummaryItem(bulkBidsItem), DataRVAdapterOperationType.Add))
        }
            userBidsData.postValue(bulkBidSummaryItemList)

    }


}

private const val BidsUpdateDelay = 1L // Delay in fetching bids after creating/updating