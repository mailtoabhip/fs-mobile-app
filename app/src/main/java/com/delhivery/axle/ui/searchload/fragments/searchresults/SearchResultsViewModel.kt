package com.delhivery.axle.ui.searchload.fragments.searchresults

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialogInterface
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import com.delhivery.axle.utils.prefs.UserPrefs
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

/**
 * View model for [SearchResultsFragment]
 */
class SearchResultsViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val bidsRepository: BidsRepository,
  val userPrefs: UserPrefs
) : BaseViewModel(), BidDetailsCreateEditDialogInterface {

  /* bid action result live data */
  var bidsActionLiveData = MutableLiveData<Pair<Int, TransactionBid>>()

  /* search results live data */
  var searchResults = MutableLiveData<List<HomeBidsRequestItemData>>()

  var loadPricePercent = 0

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
          this.loadPricePercent = t.loadPricePercent
          bidsRepository.bidsForLoads(t.transactions)
        }
        .onBackground()
        .subscribe { _tRes, error ->
          if (!error) {
            val loads = _tRes.first
            val bids = _tRes.second

            for (load in loads.toMutableList()) {
              try {
                load.loadPricePercent = loadPricePercent
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
    isPMT: Boolean,
    transactionId: String,
    bidAmount: Int,
    pmtRate: Int,
    commercialType: String,
    position: Int
  ) {
    compositeDisposable += bidsRepository.createBid(
        isPMT, transactionId, bidAmount, pmtRate, commercialType
    )
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
    isPMT: Boolean,
    transactionId: String,
    bidId: String,
    bidAmount: Int,
    pmtRate: Int,
    commercialType: String,
    position: Int
  ) {
    compositeDisposable += bidsRepository.editBid(
        isPMT, transactionId, bidId, bidAmount, commercialType,
        pmtRate
    )
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