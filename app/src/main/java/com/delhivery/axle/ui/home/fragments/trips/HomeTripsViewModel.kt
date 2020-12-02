package com.delhivery.axle.ui.home.fragments.trips

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.ExpenseRepository
import com.delhivery.axle.api.repository.LoadCycleRepository
import com.delhivery.axle.api.repository.TripsRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.repository.UserSearchLimit
import com.delhivery.axle.api.request.SearchRequest
import com.delhivery.axle.data.home.trips.HomeTripsHeaderItemData
import com.delhivery.axle.data.home.trips.TripStatus
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Update
import com.delhivery.axle.ui.bids.TripType.Completed
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import javax.inject.Inject

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * View model class for [HomeTripsFragment]
 *
 **
 */
class HomeTripsViewModel @Inject constructor(
  private val tripsRepository: TripsRepository,
  private val loadCycleRepository: LoadCycleRepository,
  private val userRepository: UserRepository,
  private val expenseRepository: ExpenseRepository
) : BaseViewModel() {

  /* user trips live data */
  var userTripsData =
    MutableLiveData<List<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* bids count live data */
  var tripsCountLiveData = MutableLiveData<Int>()

  /* data loading live data */
  var dataLoadingLiveData = MutableLiveData<Boolean>()

  var request = SearchRequest()
  var hasMoreData = true
  var offset = 0
  var total = 0

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
                              _res.advancePending,
                              _res.balancePending,
                              _res.inTransit,
                              _res.completed
                          )
                      ), Update
                  )
              )
            }
                .let { userTripsData.postValue(it) }
          }
        }
  }

  /**
   * Fetch user trips
   */
  fun fetchTrips(paginate: Boolean = false) {
    if (!paginate) {
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }

    /* add progress if not paginating */
    if (paginate) {
      Pair(HomeTripsProgressItem(), AddUpdate).let { userTripsData.postValue(listOf(it)) }
    }

    val statuses = mutableListOf<String>().apply {
      add(TripStatus.In_Transit.statusKey)
      add(TripStatus.TruckArrived.statusKey)
      add(TripStatus.TruckConfirmed.statusKey)
      add(TripStatus.TruckLoaded.statusKey)
      add(TripStatus.TruckReached.statusKey)
      add(TripStatus.TruckUnloaded.statusKey)
      add(TripStatus.EPodUploaded.statusKey)
    }
        .joinToString(separator = ",") { it }

    dataLoadingLiveData.postValue(true)

    request.offset = offset
    request.limit = UserSearchLimit
    request.tripStatus = statuses
    request.vendorId = userRepository.userId()
    compositeDisposable += loadCycleRepository.searchTrips(request.getRequest())
        .flatMap { t ->
          offset += t.trips.size
          hasMoreData = t.hasNext
          total = t.total
          tripsCountLiveData.postValue(total)
          expenseRepository.bulkExpense(t.trips)
        }
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            mutableListOf<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeTripsProgressItem(), Remove))
              val trips = _res.first
              val payments = _res.second
              Log.d("Resssss trips",""+trips)
              /* No trips found, if fresh fetch n total == 0 */
              if (total == 0) {
                add(Pair(HomeTripsWarningItem_NoLoads, AddUpdate))
              }
              /* post all trips with their respective payments as add */
              else {
                add(Pair(HomeTripsSearchItem(), AddUpdate))
                for (trip in trips) {
                  try {
                    trip.payment = payments.filter { p ->
                      p.transactionId.safeEquals(trip.transactionId)
                    }[0]
                  } catch (e: Exception) {
                    Log.d("No payment found for: ", trip.transactionId)
                  }
                  if (trip.tripStatus() == Completed) {
                    add(Pair(HomeCompletedTripItem(trip), Add))
                  } else {
                    add(Pair(HomeTripsItem(trip), Add))
                  }
                }
              }
            }
                .let {
                  userTripsData.postValue(it)
                }
          } else {
            mutableListOf<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeTripsProgressItem(), Remove))
              /* remove search item */
              add(Pair(HomeTripsSearchItem(), Remove))
              /* add api time out item */
              add(Pair(HomeTripsWarningItem_TimeOut, AddUpdate))
            }
                .let { userTripsData.postValue(it) }
          }

          dataLoadingLiveData.postValue(false)
        }
  }
}