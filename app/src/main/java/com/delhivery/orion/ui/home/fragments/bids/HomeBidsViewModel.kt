package com.delhivery.orion.ui.home.fragments.bids

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.data.home.HomeBidsRequestItemData
import com.delhivery.orion.repository.TransactionsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import javax.inject.Inject

class HomeBidsViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository
) : BaseViewModel() {

  /* transactions data */
  var transactionData = MutableLiveData<List<HomeBidsRequestItemData>>()

  var hasMoreData = true
  var offset = 0

  /**
   * Fetch user transactions
   */
  fun fetchUserTransactions(paginate: Boolean) {
    if (!paginate) {
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }
    compositeDisposable += transactionsRepository.transaction(offset)
        .onBackground()
        .progress()
        .subscribe { _tRes, error ->
          if (!error && _tRes != null) {
            offset = _tRes.offset
            hasMoreData = _tRes.offset != _tRes.total
            transactionData.postValue(_tRes.transactions)
          } else {
            error.printStackTrace()
          }
        }
  }
}