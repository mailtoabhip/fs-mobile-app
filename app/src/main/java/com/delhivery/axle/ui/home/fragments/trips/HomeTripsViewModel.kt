package com.delhivery.axle.ui.home.fragments.trips

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.data.home.trips.HomeTripsHeaderItemData
import com.delhivery.axle.data.home.trips.TripStatus
import com.delhivery.axle.repository.PaymentRepository
import com.delhivery.axle.repository.TripsRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Update
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
  private val payementRepository: PaymentRepository
) : BaseViewModel() {

  /* user trips live data */
  var userTripsData =
    MutableLiveData<List<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* bids count live data */
  var tripsCountLiveData = MutableLiveData<Int>()

  /* data loading live data */
  var dataLoadingLiveData = MutableLiveData<Boolean>()

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
          } else {
            error.handle()
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

    compositeDisposable += tripsRepository.trips(offset, statuses)
        .flatMap { t ->
          offset += t.trips.size
          hasMoreData = t.hasNext
          total = t.total
          tripsCountLiveData.postValue(total)
          payementRepository.bulkPaymentTransactions(t.trips)
        }
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            mutableListOf<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeTripsProgressItem(), Remove))

              val trips = _res.first
              val payments = _res.second

              /* No trips found, if fresh fetch n total == 0 */
              if (total == 0) {
                add(Pair(HomeTripsSearchItem(), Remove))
                add(Pair(HomeTripsWarningItem_NoLoads, AddUpdate))
              }
              /* post all trips with their respective payments as add */
              else {
                for (trip in trips) {
                  try {
                    trip.payment = payments.filter { p ->
                      p.transactionId.safeEquals(trip.transactionId)
                    }
                        .get(0)
                  } catch (e: Exception) {
                    Log.d("No payment found for: ", trip.transactionId)
                  }
                  add(Pair(HomeTripsItem(trip), Add))
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