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
import com.delhivery.axle.ui.bids.ViewPaymentType.BalancePending
import com.delhivery.axle.ui.bids.ViewPaymentType.RecoveryPending
import com.delhivery.axle.utils.DatePatterns
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonObject
import java.util.Calendar
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
  var tripIdsRecd = mutableListOf<String>()
  var searchProgress :Boolean = false

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
      add(TripStatus.TripCompleted.statusKey)
    }
        .joinToString(separator = ",") { it }

    request.offset = offset
    request.limit = UserSearchLimit
    request.vendorId = userRepository.userId()
    request.tripStatus = statuses
    request.prefix = searchText
  }

}