package com.delhivery.orion.ui.home.fragments.trips

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.data.home.trips.HomeTripsHeaderItemData
import com.delhivery.orion.data.home.trips.TripStatus
import com.delhivery.orion.repository.TripsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Update
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
   * Fetch trips summary
   */
  fun fetchTripsSummary() {
    compositeDisposable += tripsRepository.userTripsSummary()
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            mutableListOf<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              add(
                  Pair(
                      HomeTripsHeaderItem(
                          HomeTripsHeaderItemData(
                              _res.truckArrived + _res.truckConfirmed,
                              _res.truckUnloaded,
                              _res.inTransit + _res.truckLoaded + _res.truckReached,
                              _res.tripCompleted
                          )
                      ), Update
                  )
              )
            }
                .let { userTripsData.postValue(it) }
          } else {
            error.handle()
          }
        }
  }

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

    if (paginate) {
      showProgress()
      /* add progress if not paginating */
      Pair(HomeTripsProgressItem(), AddUpdate).let { userTripsData.postValue(listOf(it)) }
    }

    val statuses = mutableListOf<String>().apply {
      add(TripStatus.In_Transit.statusKey)
      add(TripStatus.TruckArrived.statusKey)
      add(TripStatus.TruckConfirmed.statusKey)
      add(TripStatus.TruckLoaded.statusKey)
      add(TripStatus.TruckReached.statusKey)
      add(TripStatus.TruckUnloaded.statusKey)
    }
        .joinToString(separator = ",") { it }

    compositeDisposable += tripsRepository.trips(offset, statuses)
        .onBackground()
        .progress()
        .subscribe { _tripsRes, error ->
          if (!error) {
            offset += _tripsRes.trips.size
            hasMoreData = _tripsRes.hasNext

            mutableListOf<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeTripsProgressItem(), Remove))

              /* edit route prefs, if fresh fetch n total == 0 */
              if (!paginate && _tripsRes.total == 0) {
                add(Pair(HomeTripsWarningItem_NoLoads, AddUpdate))
                add(Pair(HomeTripsSearchItem(), Remove))
              }
              /* post all transactions as add */
              else {
                /* post all trips as add */
                _tripsRes.trips
                    .forEach { _item ->
                      add(Pair(HomeTripsItem(_item), Add))
                    }
              }
            }
                .let {
                  userTripsData.postValue(it)

                }
          } else {
            error.handle()
          }
        }
  }
}