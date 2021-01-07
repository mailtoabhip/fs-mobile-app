package com.delhivery.axle.ui.bids

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.response.LowestBidResponse
import com.delhivery.axle.api.response.TransactionsResponse
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.exception.NoBidsFoundException
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
import io.reactivex.functions.BiFunction
import javax.inject.Inject

/**
 * View model for [BidsActivity]
 */
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
            Single.zip(
                transactionsRepository.bulkTransactions(_res.second),
                bidsRepository.bulkLowestBidsForTransactions(_res.second),
                BiFunction<Pair<List<TransactionBid>, TransactionsResponse>, List<LowestBidResponse>,
                    Triple<List<TransactionBid>, TransactionsResponse, List<LowestBidResponse>>> { t1, t2 ->
                  Triple(t1.first, t1.second, t2)
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

                var index = 0
                for (transaction in transactions) {
                  try {
                    val lowestBid = _res.third.filter { b ->
                      b.transactionId.safeEquals(
                          transaction.transactionId
                      )
                    }[0]
                    transaction.numBids = lowestBid.numBids
                    transaction.lowestBid = lowestBid.minBid
                    transaction.loadPricePercent = _res.second.loadPricePercent
                    index++
                    transaction.transactionBid = bids.filter { b ->
                      b.transactionId.safeEquals(transaction.transactionId)
                    }[0]

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