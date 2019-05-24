package com.delhivery.orion.ui.biddetails

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.data.bids.TransactionBid
import com.delhivery.orion.data.bids.TransactionBidStatus.Accepted
import com.delhivery.orion.data.bids.TransactionBidStatus.Rejected
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.repository.BidsRepository
import com.delhivery.orion.repository.TransactionsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import io.reactivex.Single
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

class BidDetailsViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val bidsRepository: BidsRepository
) : BaseViewModel(), BidDetailsCreateEditDialogInterface {

  /* transaction id */
  lateinit var transactionId: String

  /* live data */
  var transactionLiveData = MutableLiveData<HomeBidsRequestItemData>()

  var transactionBidLiveData = MutableLiveData<BidDetailsUserBidState>()

  /**
   * Fetch transaction details
   */
  fun fetchTransactionDetails() {
    compositeDisposable += transactionsRepository.transactionDetails(transactionId)
        .onBackground()
        .progress()
        .subscribe { _tRes, error ->
          if (!error) {
            transactionLiveData.postValue(_tRes)
            fetchTransactionBids()
          } else {
            error.handle()
          }
        }
  }

  /**
   * Fetch transaction bids and update UI as per response
   */
  fun fetchTransactionBids(postMessage: String? = null) {
    compositeDisposable += bidsRepository.transactionBids(transactionId)
        .onBackground()
        .bidsProgress()
        .subscribe { _bRes, error ->
          if (!error) {
            //determine bid state and post to live data
            val state = when {
              _bRes.third == 0 -> BidDetailsUserBidState_PlaceBidFirst()
              _bRes.first == null -> BidDetailsUserBidState_PlaceBid(_bRes.third, _bRes.second)
              else -> when (_bRes.first!!.status()) {
                Accepted -> BidDetailsUserBidState_ConfirmedBid(
                    transactionLiveData.value?.pickupLocation ?: "No Pickup Location"
                )
                Rejected -> BidDetailsUserBidState_RejectedBid(
                    _bRes.second.acceptedBid()!!, _bRes.first!!
                )
                else -> BidDetailsUserBidState_EditBid(_bRes.third, _bRes.second, _bRes.first!!)
              }
            }
            transactionBidLiveData.postValue(state)
          } else {
            error.handle()
          }
        }
  }

  override fun createBid(
    transactionId: String,
    bidAmount: Int
  ) {
    compositeDisposable += bidsRepository.createBid(transactionId, bidAmount)
        .delay(BidsUpdateDelay, SECONDS)
        .onBackground()
        .bidsProgress()
        .subscribe { _res, error ->
          if (!error && _res.isSuccess) {
            fetchTransactionBids(_res.responseData?.message)
          } else {
            error.handle()
          }
        }
  }

  override fun editBid(
    transactionId: String,
    bidId: String,
    bidAmount: Int
  ) {
    compositeDisposable += bidsRepository.editBid(transactionId, bidId, bidAmount)
        .delay(BidsUpdateDelay, SECONDS)
        .onBackground()
        .bidsProgress()
        .subscribe { _res, error ->
          if (!error && _res.isSuccess) {
            fetchTransactionBids(_res.responseData?.message)
          } else {
            error.handle()
          }
        }
  }

  /**
   * filter accepted Bid
   */
  private fun List<TransactionBid>.acceptedBid() =
    filter { it._status == Accepted.statusKey }.firstOrNull()

  /**
   * Emit bids fetching progress
   */
  private fun <T> Single<T>.bidsProgress() = doOnSubscribe {
    if (transactionBidLiveData.value !is BidDetailsUserBidState_LoadingBids)
      transactionBidLiveData.postValue(BidDetailsUserBidState_LoadingBids())
  }
}

private const val BidsUpdateDelay = 1L // Delay in fetching bids after creating/updating