package com.delhivery.axle.ui.biddetails

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Rejected
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.repository.BidsRepository
import com.delhivery.axle.repository.TransactionsRepository
import com.delhivery.axle.repository.TripsRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.Single
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

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
              _bRes.third == 0 -> {
                transactionBidLiveData.postValue(
                    BidDetailsUserBidState_PlaceBidFirst()
                )
                bidPriceLiveData.postValue(null)
              }
              _bRes.first.first == null -> {
                transactionBidLiveData.postValue(
                    BidDetailsUserBidState_PlaceBid(_bRes.third, _bRes.second, _bRes.first.second)
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
                            _bRes.second.acceptedBid()!!, _bRes.first.first!!
                        )
                    )
                  } catch (e: Exception) {

                  } finally {
                    bidPriceLiveData.postValue(null)
                  }
                }
                else -> {
                  transactionBidLiveData.postValue(
                      BidDetailsUserBidState_EditBid(
                          _bRes.third, _bRes.second, _bRes.first.first!!,
                          _bRes.first.second
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
    compositeDisposable += tripsRepository.tripDetails(transactionId)
        .onBackground()
        .bidsProgress()
        .subscribe { _res, error ->
          if (!error) {
            transactionBidLiveData.postValue(
                BidDetailsUserBidState_ConfirmedBid(_res.pickupLocation)
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