package com.delhivery.axle.ui.bids

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.ExpenseRepository
import com.delhivery.axle.api.repository.LoadCycleRepository
import com.delhivery.axle.api.repository.TripsRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.repository.UserSearchLimit
import com.delhivery.axle.api.request.SearchRequest
import com.delhivery.axle.data.home.trips.TripStatus
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.bids.TripType.Unknown
import com.delhivery.axle.ui.bids.ViewPaymentType.NA
import com.delhivery.axle.ui.home.fragments.trips.BaseHomeTripsRVAdapterItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsProgressItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsWarningItem_NoLoads
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsWarningItem_TimeOut
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonObject
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
  private val expenseRepository: ExpenseRepository,
  private val tripsRepository: TripsRepository,
  private val loadCycleRepository: LoadCycleRepository,
  private val userRepository: UserRepository,
  private val userPrefs: UserPrefs
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

  var request = SearchRequest()
  var tripType: TripType = Unknown
  var viewPaymentType: ViewPaymentType = NA
  var viewType: String ?= "all"
  var total = 0

  /**
   * Fetch user trips
   */
  fun fetchTrips(paginate: Boolean) {
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

    request.offset = offset
    request.limit = UserSearchLimit
    request.vendorId = userRepository.userId()
    if (viewType.equals("all")) {
      request.tripStatus = statuses
    } else if (viewType.equals("payment_view")) {
      request.tripStatus = viewPaymentType.status.joinToString(separator = ",") {it}
    } else {
      request.tripStatus = tripType.status.joinToString(separator = ",") { it }
    }
    compositeDisposable += loadCycleRepository.searchTrips(request.getRequest())
        .flatMap { t ->
          offset += t.trips.size
          hasMoreData = t.hasNext
          total = t.total
          tripsCountLiveData.postValue(total)

          val jsonObject = JsonObject()
          jsonObject.addProperty("vendor_id", userRepository.userId())
          jsonObject.addProperty("transaction_ids", t.trips.map { it.transactionId }.joinToString(",") { it })
          jsonObject.addProperty("offset", 0)
          jsonObject.addProperty("limit", 10)
          tripsRepository.bulkPayments(t.trips, jsonObject)
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
                add(Pair(HomeTripsWarningItem_NoLoads, AddUpdate))
              }
              /* post all trips with their respective payments as add */
              else {
                for (trip in trips) {
                  trip.tds = userPrefs.tdsRate
                  trip.updatedTds = userPrefs.updatedTdsRate
                  try {
                    trip.payment = payments.filter { p ->
                      p.transactionId.safeEquals(trip.transactionId)
                    }[0]
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
              /* add api time out item */
              add(Pair(HomeTripsWarningItem_TimeOut, AddUpdate))
            }
                .let { userTripsData.postValue(it) }
          }

          dataLoadingLiveData.postValue(false)
        }
  }

}