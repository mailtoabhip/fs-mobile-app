package com.delhivery.orion.ui.bids

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.delhivery.orion.repository.PaymentRepository
import com.delhivery.orion.repository.TripsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.ui.bids.TripType.Unknown
import com.delhivery.orion.ui.home.fragments.trips.BaseHomeTripsRVAdapterItem
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsItem
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsProgressItem
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsSearchItem
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsWarningItem_NoLoads
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsWarningItem_TimeOut
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import com.delhivery.orion.utils.extensions.safeEquals
import javax.inject.Inject

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * View model class for [TripsActivity]
 *
 **
 */
class TripsViewModel @Inject constructor(
  private val tripsRepository: TripsRepository,
  private val payementRepository: PaymentRepository
) : BaseViewModel() {
  /* user trips live data */
  var userTripsData =
    MutableLiveData<List<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* trips count live data */
  var tripsCountLiveData = MutableLiveData<Int>()

  /* data loading live data */
  var dataLoadingLiveData = MutableLiveData<Boolean>()

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

    if (paginate) {
      showProgress()
      /* add progress if not paginating */
      Pair(HomeTripsProgressItem(), AddUpdate).let { userTripsData.postValue(listOf(it)) }
    }

    dataLoadingLiveData.postValue(true)

    compositeDisposable += tripsRepository.trips(
        offset, trip.status.joinToString(separator = ",") { it }
    )
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
              /* add refresh list item */
              add(Pair(HomeTripsWarningItem_TimeOut, AddUpdate))
            }
                .let { userTripsData.postValue(it) }

            error.handle()
          }

          dataLoadingLiveData.postValue(false)
        }
  }

}