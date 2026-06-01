package com.delhivery.axle.ui.home.fragments.pod

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadCycleRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.repository.UserSearchLimit
import com.delhivery.axle.api.request.SearchRequest
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.data.home.trips.PodCounts
import com.delhivery.axle.data.home.trips.TripStatus
import com.delhivery.axle.data.home.trips.TripStatus.EPodUploaded
import com.delhivery.axle.data.home.trips.TripStatus.TruckUnloaded
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.utils.DatePatterns.OrionDateFormat
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import java.io.File
import java.util.Calendar
import javax.inject.Inject

/**
 * View model class for [SubmittedPodTabFragment]
 */
class SubmittedPodViewModel @Inject constructor(
  private val userRepository: UserRepository,
  private val loadCycleRepository: LoadCycleRepository
) : BaseViewModel() {

  /* user trips live data */
  var userPodsData =
    MutableLiveData<List<Pair<BaseHomePodRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* trips count live data */
  var tripsCountLiveData = MutableLiveData<Int>()
  var dataLoadingLiveData = MutableLiveData<Boolean>()
  var delegationLiveData = MutableLiveData<Triple<DelegationToken, String, File>>()
  
  /* pod counts live data */
  var podCountsLiveData = MutableLiveData<PodCounts>()

  var request = SearchRequest()
  var status: TripStatus = EPodUploaded
  var dispatch: Boolean = true // Default to true for submitted POD
  var transactionId: String = ""
  var podUrl: String = ""
  var empty = true

  /* pagination params */
  var total = 0
  var offset = 0
  var hasMoreData = true

  /**
   * Cancel ongoing fetch requests
   */
  fun cancelOngoingRequests() {
    compositeDisposable.clear()
    dataLoadingLiveData.postValue(false)
  }

  /**
   * Fetch trips for submitted POD
   */
  fun fetchTrips(paginate: Boolean = false) {
    if (!paginate) {
      empty = true
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }

    /* add progress if not paginating */
    if (paginate) {
      Pair(HomePodProgressItem(), AddUpdate).let { userPodsData.postValue(listOf(it)) }
    }

    dataLoadingLiveData.postValue(true)
    request.offset = offset
    request.limit = UserSearchLimit
    request.vendorId = userRepository.userId()
    // For submitted POD, we always use EPodUploaded status
    request.tripStatus = EPodUploaded.statusKey + "," + TruckUnloaded.statusKey
    request.value = null
  }



}

