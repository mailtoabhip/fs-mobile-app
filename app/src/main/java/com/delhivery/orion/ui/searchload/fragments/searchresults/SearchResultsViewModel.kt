package com.delhivery.orion.ui.searchload.fragments.searchresults

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.delhivery.orion.data.CityModel
import com.delhivery.orion.data.bids.TransactionBid
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.repository.BidsRepository
import com.delhivery.orion.repository.TransactionsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.biddetails.BidDetailsCreateEditDialogInterface
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import com.delhivery.orion.utils.extensions.safeEquals
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

class SearchResultsViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val bidsRepository: BidsRepository
) : BaseViewModel(), BidDetailsCreateEditDialogInterface {

  /* bid action result live data */
  var bidsActionLiveData = MutableLiveData<Pair<Int, TransactionBid>>()

  /* search results live data */
  var searchResults = MutableLiveData<List<HomeBidsRequestItemData>>()

  /**
   * Search load api
   */
  fun searchLoad(
    origin: CityModel,
    destination: CityModel?,
    type: String
  ) {
    /* dummy data */
    compositeDisposable += transactionsRepository.searchTransactions(
        0, origin.cityId, destination?.cityId, type.toLowerCase()
    )
        .flatMap { t ->
          bidsRepository.bidsForLoads(t.transactions)
        }
        .onBackground()
        .subscribe { _tRes, error ->
          if (!error) {
            val loads = _tRes.first
            val bids = _tRes.second

            for (load in loads.toMutableList()) {
              try {
                load.transactionBid =
                  bids.filter { b ->
                    b.transactionId.safeEquals(load.transactionId)
                  }
                      .get(0)
              } catch (e: Exception) {
                Log.d("No Bid found for: ", load.transactionId)
              }
            }
            searchResults.postValue(loads)
          } else {
            searchResults.postValue(null)
            error.handle()
          }
        }
  }

  override fun createBid(
    transactionId: String,
    bidAmount: Int,
    position: Int
  ) {
    compositeDisposable += bidsRepository.createBid(transactionId, bidAmount)
        .delay(BidsUpdateDelay, SECONDS)
        .flatMap {
          bidsRepository.transactionBid(transactionId)
        }
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            bidsActionLiveData.postValue(Pair(position, _res))
          } else {
            error.handle()
          }
        }
  }

  override fun editBid(
    transactionId: String,
    bidId: String,
    bidAmount: Int,
    position: Int
  ) {
    compositeDisposable += bidsRepository.editBid(transactionId, bidId, bidAmount)
        .delay(BidsUpdateDelay, SECONDS)
        .flatMap {
          bidsRepository.transactionBid(transactionId)
        }
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            bidsActionLiveData.postValue(Pair(position, _res))
          } else {
            error.handle()
          }
        }
  }
}

private const val BidsUpdateDelay = 1L