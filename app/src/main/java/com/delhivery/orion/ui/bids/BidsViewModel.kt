package com.delhivery.orion.ui.bids

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.exception.NoBidsFoundException
import com.delhivery.orion.repository.BidsRepository
import com.delhivery.orion.repository.TransactionsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.orion.ui.bids.BidType.Unknown
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRequestItem
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
    MutableLiveData<List<Pair<HomeBidsRequestItem, DataRVAdapterOperationType>>>()

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

    compositeDisposable += bidsRepository.userBids(bidType.status, offset)
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
//    compositeDisposable += transactionsRepository.transactions(0, Requested)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error) {
            mutableListOf<Pair<HomeBidsRequestItem, DataRVAdapterOperationType>>().apply {
              _res.transactions.forEach { _item ->
                add(Pair(HomeBidsRequestItem(_item), Add))
              }
            }
                .let {
                  bidsLiveData.postValue(it)
                }
          } else {
            if (error is NoBidsFoundException) {
              bidsLiveData.postValue(null)
            } else {
              error.handle()
            }
          }
        }
  }
}