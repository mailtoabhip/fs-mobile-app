package com.delhivery.orion.ui.searchload.fragments.searchresults

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.data.CityModel
import com.delhivery.orion.data.home.HomeBidsRequestItemData
import com.delhivery.orion.repository.TransactionsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import javax.inject.Inject

class SearchResultsViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository
) : BaseViewModel() {

  /* search results live data */
  var searchResults = MutableLiveData<List<HomeBidsRequestItemData>>()

  /**
   * Search load api
   */
  fun searchLoad(
    origin: CityModel,
    destination: CityModel,
    type: String,
    size: String
  ) {
    /* dummy data */
    compositeDisposable += transactionsRepository.searchTransactions(
        0, origin.cityId, destination.cityId
    )
        .onBackground()
        .subscribe { _tRes, error ->
          if (!error) {
            searchResults.postValue(_tRes.transactions)
          } else {
            searchResults.postValue(null)
            error.handle()
          }
        }
  }
}