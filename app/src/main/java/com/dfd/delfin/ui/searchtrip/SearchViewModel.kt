package com.dfd.delfin.ui.searchtrip

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.LoadCycleRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.api.repository.UserSearchLimit
import com.dfd.delfin.api.request.SearchRequest
// Removed AWS imports - using Document API now
import com.dfd.delfin.data.home.trips.TripStatus.EPodUploaded
import com.dfd.delfin.data.home.trips.TripStatus.TruckUnloaded
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.dfd.delfin.utils.prefs.UserPrefs
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