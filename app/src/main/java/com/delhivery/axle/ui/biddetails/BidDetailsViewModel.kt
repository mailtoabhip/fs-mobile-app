package com.delhivery.axle.ui.biddetails

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.repository.TripsRepository
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Rejected
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.EVENT_BID_INLINE_PROMPT
import com.delhivery.axle.utils.EVENT_BID_REVISE_PROMPT
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
  val userPrefs: UserPrefs
) : BaseViewModel(), BidDetailsCreateEditDialogInterface {

  /* transaction id */
  lateinit var transactionId: String

  /* live data */
  var transactionLiveData = MutableLiveData<HomeBidsRequestItemData>()

  var transactionBidLiveData = MutableLiveData<BidDetailsUserBidState>()

  var bidPriceLiveData = MutableLiveData<TransactionBid>()

    var analyticsBucket :Boolean = false

    lateinit var transaction: HomeBidsRequestItemData


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
}

private const val BidsUpdateDelay = 1L // Delay in fetching bids after creating/updating