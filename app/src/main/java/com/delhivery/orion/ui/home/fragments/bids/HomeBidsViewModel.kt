package com.delhivery.orion.ui.home.fragments.bids

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.api.response.BidSummaryResponse
import com.delhivery.orion.data.bids.TransactionBid
import com.delhivery.orion.data.home.bids.HomeBidsHeaderItemData
import com.delhivery.orion.exception.NoBidsFoundException
import com.delhivery.orion.repository.BidsRepository
import com.delhivery.orion.repository.TransactionsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Update
import com.delhivery.orion.utils.extensions.convertResponse
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class HomeBidsViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val bidsRepository: BidsRepository
) : BaseViewModel() {

  /* user bids live data */
  var userBidsData =
    MutableLiveData<List<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  var bidsCountLiveData = MutableLiveData<Int>()

  /* bid type */
  lateinit var bidSummary: BidSummaryResponse

  /* pagination params */
  var total = 0
  var offset = 0

  /**
   * Fetch bids
   */
  fun fetchBids(paginate: Boolean = false) {
    if (!paginate) {
      offset = 0
    } else if (paginate && (total == offset)) {
      return
    }

    if (paginate) {
      showProgress()
      /* add progress if not paginating */
      Pair(HomeBidsProgressItem(), AddUpdate).let { userBidsData.postValue(listOf(it)) }
    }

    compositeDisposable += Single.zip(
        bidsRepository.userBidsSummary(), bidsRepository.userBids(offset),
        BiFunction<BidSummaryResponse, Pair<Int, List<TransactionBid>>,
            Pair<BidSummaryResponse, Pair<Int, List<TransactionBid>>>> { t1, t2 ->
          Pair(t1, t2)
        })
        .flatMap { t ->
          offset += t.second.second.size
          total = t.second.first
          bidsCountLiveData.postValue(total)
          bidSummary = t.first
          if (!paginate && total == 0) {
            Single.error(NoBidsFoundException())
          } else {
            transactionsRepository.bulkTransactions(t.second.second.map { it.transactionId })
                .convertResponse()
          }
        }
        .onBackground()
        .progress()
        .subscribe { _data, error ->
          if (!error) {
            mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeBidsProgressItem(), Remove))

              add(
                  Pair(
                      HomeBidsHeaderItem(
                          HomeBidsHeaderItemData(
                              bidSummary.myBids,
                              bidSummary.confirmedBids,
                              bidSummary.lostBids
                          )
                      ), Update
                  )
              )

              /* edit route prefs, if fresh fetch n total == 0 */
              if (!paginate && _data.total == 0) {
                add(Pair(HomeBidsWarningItem_NoBids, AddUpdate))
              }
              /* post all transactions as add */
              else {
                _data.transactions.forEach { _item ->
                  add(Pair(HomeBidsRequestItem(_item), Add))
                }
              }
            }
                .let { userBidsData.postValue(it) }
          } else {
            error.handle()
            showProgress(false)
          }
        }
  }

}