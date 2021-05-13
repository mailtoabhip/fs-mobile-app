package com.delhivery.axle.ui.searchongoingtrip

import android.util.Log
import androidx.lifecycle.MutableLiveData
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
import com.delhivery.axle.ui.bids.TripType
import com.delhivery.axle.ui.bids.TripType.Unknown
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonObject
import javax.inject.Inject

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 12/5/21
 */

class SearchOngoingTripViewModel @Inject constructor(
  private val tripsRepository: TripsRepository,
  private val loadCycleRepository: LoadCycleRepository,
  private val userRepository: UserRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  /* user trips live data */
  var searchLiveData =
    MutableLiveData<List<Pair<BaseSearchOngoingTripRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* data loading live data */
  var dataLoadingLiveData = MutableLiveData<Boolean>()

  /* pagination params */
  var hasMoreData = true
  var offset = 0
  var total = 0

  var searchText = ""
  var tripType: TripType = Unknown
  var request = SearchRequest()

  fun searchTrips(paginate: Boolean = false) {

    if (!paginate) {
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }

    showProgress()
    /* add progress if not paginating */
    Pair(SearchProgressItem(), AddUpdate).let { searchLiveData.postValue(listOf(it)) }

    dataLoadingLiveData.postValue(true)

    val statuses = mutableListOf<String>().apply {
      add(TripStatus.TruckConfirmed.statusKey)
      add(TripStatus.TruckArrived.statusKey)
      add(TripStatus.TruckLoaded.statusKey)
      add(TripStatus.In_Transit.statusKey)
      add(TripStatus.TruckReached.statusKey)
      add(TripStatus.TruckUnloaded.statusKey)
      add(TripStatus.EPodUploaded.statusKey)
    }
        .joinToString(separator = ",") { it }

    request.offset = offset
    request.limit = UserSearchLimit
    request.vendorId = userRepository.userId()
    request.tripStatus = statuses
    request.prefix = searchText
    compositeDisposable += loadCycleRepository.searchTrips(request.getRequest())
        .flatMap { t ->
          offset += t.trips.size
          hasMoreData = t.hasNext
          total = t.total

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
            mutableListOf<Pair<BaseSearchOngoingTripRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(SearchProgressItem(), Remove))
              val trips = _res.first
              val payments = _res.second

              /* No trips found, if fresh fetch n total == 0 */
              if (total == 0) {
                 add(Pair(SearchOngoingTripWarningItem_NoResult, AddUpdate))
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
                  add(Pair(SearchDataItem(trip), Add))
                }
              }
            }
                .let {
                  searchLiveData.postValue(it)
                }
          } else {
            mutableListOf<Pair<BaseSearchOngoingTripRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
               add(Pair(SearchProgressItem(), Remove))
              /* add api time out item */
               add(Pair(SearchOngoingTripsWarningItem_NoResult, AddUpdate))
            }
                .let { searchLiveData.postValue(it) }
          }

          dataLoadingLiveData.postValue(false)
        }
  }

}