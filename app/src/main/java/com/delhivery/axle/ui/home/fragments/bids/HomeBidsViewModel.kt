package com.delhivery.axle.ui.home.fragments.bids

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.data.home.bids.HomeBidsHeaderItemData
import com.delhivery.axle.exception.NoBidsFoundException
import com.delhivery.axle.repository.BidsRepository
import com.delhivery.axle.repository.TransactionsRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Update
import com.delhivery.axle.ui.bids.BidType
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import io.reactivex.Single
import javax.inject.Inject

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
  private val bidsRepository: BidsRepository
) : BaseViewModel() {

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

  /**
   * Fetch bids summary
   */
  fun fetchBidsSummary() {
    compositeDisposable += bidsRepository.userBidsSummary()
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(
                  Pair(
                      HomeBidsHeaderItem(
                          HomeBidsHeaderItemData(
                              _res.myBids,
                              _res.confirmedBids,
                              _res.lostBids
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
   */
  fun fetchBids(paginate: Boolean = false) {
    if (!paginate) {
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }

    /* add progress if not paginating */
    if (paginate) {
      Pair(HomeBidsProgressItem(), AddUpdate).let { userBidsData.postValue(listOf(it)) }
    }

    val statuses = mutableListOf<String>().apply {
      add(BidType.ActiveBid.status.statusKey)
      add(BidType.ConfirmedBid.status.statusKey)
    }
        .joinToString(separator = ",") { it }

    dataLoadingLiveData.postValue(true)

    compositeDisposable += bidsRepository.userBids(offset, statuses)
        .flatMap { _res ->
          total = _res.first
          bidsCountLiveData.postValue(total)
          if (!paginate && total == 0) {
            Single.error(NoBidsFoundException())
          } else {
            transactionsRepository.bulkTransactions(_res.second)
          }
        }
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            offset += _res.second.offset
            hasMoreData = _res.second.hasNext

            mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeBidsProgressItem(), Remove))

              /* edit route prefs, if fresh fetch n total == 0 */
              if (!paginate && total == 0) {
                add(Pair(HomeBidsWarningItem_NoBids, AddUpdate))
              }
              /* post all transactions mapped to bids as add */
              else {
                val bids = _res.first
                val transactions = _res.second.transactions

                for (transaction in transactions) {
                  try {
                    transaction.loadPricePercent = _res.second.loadPricePercent
                    transaction.transactionBid = bids.filter { b ->
                      b.transactionId.safeEquals(transaction.transactionId)
                    }
                        .get(0)

                  } catch (e: Exception) {
                    Log.d("No Bid found for: ", transaction.transactionId)
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

}