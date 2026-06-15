package com.dfd.delfin.ui.searchongoingtrip

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.LoadCycleRepository
import com.dfd.delfin.api.repository.TripsRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.api.repository.UserSearchLimit
import com.dfd.delfin.api.request.SearchRequest
import com.dfd.delfin.data.home.trips.TripStatus
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.dfd.delfin.ui.bids.TripType
import com.dfd.delfin.ui.bids.TripType.Unknown
import com.dfd.delfin.utils.prefs.UserPrefs
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