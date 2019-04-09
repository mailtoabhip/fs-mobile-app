package com.delhivery.orion.ui.biddetails

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.data.home.HomeBidsRequestItemData
import com.delhivery.orion.repository.TransactionsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import javax.inject.Inject

class BidDetailsViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository
) : BaseViewModel() {

  /* transaction id */
  lateinit var transactionId: String

  /* live data */
  var transactionLiveData = MutableLiveData<HomeBidsRequestItemData>()

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
          } else {
            error.printStackTrace()
          }
        }
  }
}