package com.dfd.delfin.ui.home.fragments.pod

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.LoadCycleRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.api.repository.UserSearchLimit
import com.dfd.delfin.api.request.SearchRequest
import com.dfd.delfin.api.response.DelegationToken
import com.dfd.delfin.data.home.trips.PodCounts
import com.dfd.delfin.data.home.trips.TripStatus
import com.dfd.delfin.data.home.trips.TripStatus.EPodUploaded
import com.dfd.delfin.data.home.trips.TripStatus.TruckUnloaded
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import java.io.File
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

