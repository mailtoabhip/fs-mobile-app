package com.delhivery.axle.ui.bids

import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.delhivery.axle.exception.NoBidsFoundException
import com.delhivery.axle.repository.BidsRepository
import com.delhivery.axle.repository.TransactionsRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
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
import javax.inject.Inject

class BidsViewModel @Inject constructor(
  private val bidsRepository: BidsRepository,
  private val transactionsRepository: TransactionsRepository
) : BaseViewModel() {

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

    compositeDisposable += bidsRepository.userBidsByStatus(bidType.status, offset)
        .flatMap { _res ->
          total = _res.first
          bidsCountLiveData.postValue(total)
          if (!paginate && _res.first == 0) {
            Single.error(NoBidsFoundException())
          } else {
            transactionsRepository.bulkTransactions(_res.second)
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

                for (transaction in transactions) {
                  try {
                    transaction.targetPricePercent = _res.second.loadPricePercent
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
}