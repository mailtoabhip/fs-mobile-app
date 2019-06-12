package com.delhivery.orion.ui.bids

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.exception.NoBidsFoundException
import com.delhivery.orion.repository.BidsRepository
import com.delhivery.orion.repository.TransactionsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.ui.bids.BidType.Unknown
import com.delhivery.orion.ui.home.fragments.bids.BaseHomeBidsRVAdapterItem
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsProgressItem
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRequestItem
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsWarningItem_NoBids
import com.delhivery.orion.utils.extensions.convertResponse
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import io.reactivex.Single
import javax.inject.Inject

class BidsViewModel @Inject constructor(
  private val bidsRepository: BidsRepository,
  private val transactionsRepository: TransactionsRepository
) : BaseViewModel() {

  /* Bids live data */
  var bidsLiveData =
    MutableLiveData<List<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* bid type */
  var bidType: BidType = Unknown

  /* pagination params */
  var total = 0
  var offset = 0

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

    compositeDisposable += bidsRepository.userBidsByStatus(bidType.status, offset)
        .flatMap { _bidsRes ->
          offset += _bidsRes.second.size
          total = _bidsRes.first
          if (!paginate && total == 0) {
            Single.error(NoBidsFoundException())
          } else {
            transactionsRepository.bulkTransactions(_bidsRes.second.map { it.transactionId })
                .convertResponse()
          }
        }
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error) {
            mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeBidsProgressItem(), Remove))

              if (!paginate && _res.total == 0) {
                add(Pair(HomeBidsWarningItem_NoBids, AddUpdate))
              } else {
                _res.transactions.forEach { _item ->
                  add(Pair(HomeBidsRequestItem(_item), Add))
                }
              }
            }
                .let {
                  bidsLiveData.postValue(it)
                }
          } else {
            if (error is NoBidsFoundException) {
              mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                /* remove progress item */
                add(Pair(HomeBidsProgressItem(), Remove))
                /* add no bids warning item */
                add(Pair(HomeBidsWarningItem_NoBids, AddUpdate))
              }
                  .let { bidsLiveData.postValue(it) }
            } else {
              error.handle()
            }
          }
        }
  }
}