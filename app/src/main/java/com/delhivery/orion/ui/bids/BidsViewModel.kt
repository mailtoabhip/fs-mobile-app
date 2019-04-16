package com.delhivery.orion.ui.bids

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.data.bids.TransactionBid
import com.delhivery.orion.repository.BidsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.bids.BidType.Unknown
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import javax.inject.Inject

class BidsViewModel @Inject constructor(
  private val bidsRepository: BidsRepository
) : BaseViewModel() {

  /* Bids live data */
  var bidsLiveData = MutableLiveData<Pair<Int, List<TransactionBid>>>()

  /* bid type */
  var bidType: BidType = Unknown

  /**
   * Fetch bids
   */
  fun fetchBids() {
    if (bidType == Unknown) return

    compositeDisposable += bidsRepository.userBids(bidType.status, 0)
        .onBackground()
        .progress()
        .subscribe { _bidsRes, error ->
          if (!error) {
            bidsLiveData.postValue(_bidsRes)
          } else {
            error.handle()
          }
        }
  }
}