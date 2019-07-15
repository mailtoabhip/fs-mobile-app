package com.delhivery.orion.ui.biddetails

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.data.bids.TransactionBid
import com.delhivery.orion.data.bids.TransactionBidStatus.Accepted
import com.delhivery.orion.data.bids.TransactionBidStatus.Rejected
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.data.home.trips.TripDriverDetails
import com.delhivery.orion.repository.BidsRepository
import com.delhivery.orion.repository.TransactionsRepository
import com.delhivery.orion.repository.TripsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import io.reactivex.Single
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

class BidDetailsViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val bidsRepository: BidsRepository,
  private val tripsRepository: TripsRepository
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
  private fun fetchTransactionBids(postMessage: String? = null) {
    compositeDisposable += bidsRepository.transactionBids(transactionId)
        .onBackground()
        .bidsProgress()
        .subscribe { _bRes, error ->
          if (!error) {
            //determine bid state and post to live data
            when {
              _bRes.third == 0 -> transactionBidLiveData.postValue(
                  BidDetailsUserBidState_PlaceBidFirst()
              )
              _bRes.first == null -> transactionBidLiveData.postValue(
                  BidDetailsUserBidState_PlaceBid(_bRes.third, _bRes.second)
              )
              else -> when (_bRes.first!!.status()) {
                Accepted -> fetchTripDetails()
                Rejected -> transactionBidLiveData.postValue(
                    BidDetailsUserBidState_RejectedBid(
                        _bRes.second.acceptedBid()!!, _bRes.first!!
                    )
                )
                else -> transactionBidLiveData.postValue(
                    BidDetailsUserBidState_EditBid(_bRes.third, _bRes.second, _bRes.first!!)
                )
              }
            }
          } else {
            error.handle()
          }
        }
  }

  private fun fetchTripDetails() {
    compositeDisposable += tripsRepository.tripDetails(transactionId)
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
    transactionId: String,
    bidAmount: Int,
    position: Int
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
    bidAmount: Int,
    position: Int
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