package com.delhivery.orion.ui.biddetails

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.data.home.HomeBidsRequestItemData
import com.delhivery.orion.repository.BidsRepository
import com.delhivery.orion.repository.TransactionsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
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
            error.printStackTrace()
          }
        }
  }

  /**
   * Fetch transaction bids and update UI as per response
   */
  fun fetchTransactionBids() {
    compositeDisposable += bidsRepository.transactionBids(transactionId)
        .onBackground()
        .subscribe { _bRes, error ->
          if (!error) {
            //determine bid state and post to live data
            val state = when {
              _bRes.third == 0 -> BidDetailsUserBidState_PlaceBidFirst()
              _bRes.first == null -> BidDetailsUserBidState_PlaceBid(_bRes.third, _bRes.second)
              else -> BidDetailsUserBidState_EditBid(_bRes.third, _bRes.second, _bRes.first!!)
            }
            transactionBidLiveData.postValue(state)
          } else {
            error.printStackTrace()
          }
        }
  }

  override fun createBid(
    transactionId: String,
    bidAmount: Int
  ) {
    compositeDisposable += bidsRepository.createBid(transactionId, bidAmount)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res.isSuccess) {
            /* show message snackbar */
            fetchTransactionBids()
          } else {
            error.printStackTrace()
          }
        }
  }

  override fun editBid(
    transactionId: String,
    bidId: String,
    bidAmount: Int
  ) {
    compositeDisposable += bidsRepository.editBid(transactionId, bidId, bidAmount)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res.isSuccess) {
            fetchTransactionBids()
          } else {
            error.printStackTrace()
          }
        }
  }
}