package com.delhivery.axle.ui.searchtrip

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadCycleRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.repository.UserSearchLimit
import com.delhivery.axle.api.request.SearchRequest
// Removed AWS imports - using Document API now
import com.delhivery.axle.data.home.trips.TripStatus.EPodUploaded
import com.delhivery.axle.data.home.trips.TripStatus.TruckUnloaded
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.io.File
import javax.inject.Inject

/**
 * View model for [SearchActivity]
 */
class SearchViewModel @Inject constructor(
  private val loadCycleRepository: LoadCycleRepository,
  private val userRepository: UserRepository,
  val userPrefs: UserPrefs
) : BaseViewModel() {
  
  // LiveData to track when search list is shown with data
  var searchListShownTracked = MutableLiveData<Boolean>().apply { value = false }

  var searchLiveData =
    MutableLiveData<List<Pair<BaseSearchRVAdapterItem<*>, DataRVAdapterOperationType>>>()
  var dataLoadingLiveData = MutableLiveData<Boolean>()
  var podDownloadLiveData = MutableLiveData<Pair<String, File>>()

  var request = SearchRequest()
  var transactionId: String = ""
  var podUrl: String = ""

  var total = 0
  var offset = 0
  var hasMoreData = true

  /**
   * Search trips basis transactionId, lr,
   */
  fun searchTrips(paginate: Boolean = false) {
    if (!paginate) {
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }

    if (paginate) Pair(SearchProgressItem(), AddUpdate).let { searchLiveData.postValue(listOf(it)) }
    dataLoadingLiveData.postValue(true)
    request.offset = offset
    request.limit = UserSearchLimit
    request.tripStatus = EPodUploaded.statusKey + "," + TruckUnloaded.statusKey
    request.vendorId = userRepository.userId()
  }

  /**
   * Prepare download for POD using direct URL
   */
  fun prepareDownload(podUrl: String, file: File) {
    podDownloadLiveData.postValue(Pair(podUrl, file))
  }

}