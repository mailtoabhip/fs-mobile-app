package com.delhivery.axle.ui.bids

import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.delhivery.axle.repository.PaymentRepository
import com.delhivery.axle.repository.TripsRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.bids.TripType.Unknown
import com.delhivery.axle.ui.home.fragments.trips.BaseHomeTripsRVAdapterItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsProgressItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsSearchItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsWarningItem_NoLoads
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsWarningItem_TimeOut
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
              /* add api time out item */
              add(Pair(HomeTripsWarningItem_TimeOut, AddUpdate))
            }
                .let { userTripsData.postValue(it) }
          }

          dataLoadingLiveData.postValue(false)
        }
  }

}