package com.delhivery.orion.ui.home.fragments.trips

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.repository.TripsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import javax.inject.Inject

class HomeTripsViewModel @Inject constructor(
  private val tripsRepository: TripsRepository
) : BaseViewModel() {

  /* user trips live data */
  var userTripsData =
    MutableLiveData<List<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  var hasMoreData = true
  var offset = 0

  /**
   * Fetch user trips
   */
  fun fetchTrips(paginate: Boolean) {
    if (!paginate) {
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }

    compositeDisposable += tripsRepository.trips(offset)
        .onBackground()
        .progress()
        .subscribe { _tripsRes, error ->
          if (!error) {
            offset += _tripsRes.trips.size
            hasMoreData = _tripsRes.hasNext

            if (!paginate && _tripsRes.total == 0) {
              /* show no trips error */
              userTripsData.postValue(null)
            } else {
              mutableListOf<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                /* post all trips as add */
                _tripsRes.trips.forEach { _item ->
                  add(Pair(HomeTripsItem(_item), Add))
                }
              }
                  .let {
                    userTripsData.postValue(it)
                  }
            }
          } else {
            error.handle()
          }
        }
  }
}