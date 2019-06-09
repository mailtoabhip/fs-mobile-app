package com.delhivery.orion.ui.bids

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.repository.TripsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.ui.bids.TripType.Unknown
import com.delhivery.orion.ui.home.fragments.trips.BaseHomeTripsRVAdapterItem
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsItem
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsProgressItem
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import javax.inject.Inject

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * <Define what the class does>
 *
 **
 */
class TripsViewModel @Inject constructor(
  private val tripsRepository: TripsRepository
) : BaseViewModel() {
  /* user trips live data */
  var userTripsData =
    MutableLiveData<List<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* pagination params */
  var hasMoreData = true
  var offset = 0

  var trip: TripType = Unknown
  var total: Int = 0

  /**
   * Fetch user trips
   */
  fun fetchTrips(
    paginate: Boolean
  ) {
    if (!paginate) {
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }

    compositeDisposable += tripsRepository.trips(offset, trip.status)
        .onBackground()
        .progress()
        .subscribe { _tripsRes, error ->
          if (!error) {
            offset += _tripsRes.trips.size
            hasMoreData = _tripsRes.hasNext
            total = _tripsRes.total

            if (!paginate && _tripsRes.total == 0) {
              /* show no trips error */
              userTripsData.postValue(null)
            } else {
              mutableListOf<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                /* remove progress item */
                add(Pair(HomeTripsProgressItem(), Remove))
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