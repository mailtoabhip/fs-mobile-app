package com.delhivery.orion.ui.home.fragments.bids

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.data.home.bids.HomeBidsHeaderItemData
import com.delhivery.orion.repository.BidsRepository
import com.delhivery.orion.repository.TransactionsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Update
import com.delhivery.orion.ui.bids.BidType
import com.delhivery.orion.utils.extensions.convertResponse
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import javax.inject.Inject

class HomeBidsViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val bidsRepository: BidsRepository
) : BaseViewModel() {

  /* user bids live data */
  var userBidsData =
    MutableLiveData<List<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  var bidsCountLiveData = MutableLiveData<Int>()

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

    compositeDisposable += bidsRepository.userBids(offset, statuses)
        .flatMap { t ->
          total = t.first
          bidsCountLiveData.postValue(total)
//          if (!paginate && total == 0) {
//            Single.error(NoBidsFoundException())
//          } else {
          transactionsRepository.bulkTransactions(t.second.map { it.transactionId })
              .convertResponse()
//          }
        }
        .onBackground()
        .subscribe { _data, error ->
          if (!error) {
            offset += _data.offset
            hasMoreData = _data.hasNext

            mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeBidsProgressItem(), Remove))

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