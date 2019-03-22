package com.delhivery.orion.ui.searchload.fragments.searchresults

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.home.fragments.bids.BaseHomeBidsRVAdapterItem
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRequestItem
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsSearchSpinnerItem
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

class SearchResultsViewModel @Inject constructor() : BaseViewModel() {

  /* search results live data */
  var searchResults = MutableLiveData<List<BaseHomeBidsRVAdapterItem<*>>>()

  /**
   * Search load api
   */
  fun searchLoad(
    origin: String,
    destination: String,
    type: String,
    size: String
  ) {
    /* dummy data */
    compositeDisposable += Single.zip(
        Single.timer(500, MILLISECONDS),
        Single.fromCallable {
          mutableListOf<BaseHomeBidsRVAdapterItem<*>>().apply {
            add(0, HomeBidsSearchSpinnerItem())
            for (i in 0..20) {
              add(i + 1, HomeBidsRequestItem())
            }
          }
        },
        BiFunction<Any, List<BaseHomeBidsRVAdapterItem<*>>, List<BaseHomeBidsRVAdapterItem<*>>> { _, items -> items }
    )
        .onBackground()
        .subscribe { items, error ->
          if (!error) {
            searchResults.postValue(items)
          } else {
            searchResults.postValue(null)
            error.printStackTrace()
          }
        }
  }
}