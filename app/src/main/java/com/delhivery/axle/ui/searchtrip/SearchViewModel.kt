package com.delhivery.axle.ui.searchtrip

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.request.SearchRequest
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.data.home.trips.TripStatus.EPodUploaded
import com.delhivery.axle.data.home.trips.TripStatus.TruckUnloaded
import com.delhivery.axle.repository.LoadCycleRepository
import com.delhivery.axle.repository.UserRepository
import com.delhivery.axle.repository.UserSearchLimit
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

  var searchLiveData =
    MutableLiveData<List<Pair<BaseSearchRVAdapterItem<*>, DataRVAdapterOperationType>>>()
  var dataLoadingLiveData = MutableLiveData<Boolean>()
  var delegationLiveData = MutableLiveData<Triple<DelegationToken, String, File>>()

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
    compositeDisposable += loadCycleRepository.searchTrips(request.getRequest())
        .onBackground()
        .subscribe { res, error ->
          if (!error) {
            offset += res.trips.size
            hasMoreData = res.hasNext
            total = res.total
            mutableListOf<Pair<BaseSearchRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              add(Pair(SearchProgressItem(), Remove))
              if (!paginate && total > 0) {
                add(
                    Pair(
                        SearchedQueryItem(
                            SearchRequest(
                                vehicleNumber = request.vehicleNumber, lr = request.lr,
                                result = total
                            )
                        ), Add
                    )
                )
              }

              if (total == 0) {
                add(Pair(SearchWarningItem_NoResult, AddUpdate))
              } else {
                for (trip in res.trips) {
                  add(Pair(SearchDataItem(trip), Add))
                }
              }
            }
                .let {
                  searchLiveData.postValue(it)
                }
          } else {
            mutableListOf<Pair<BaseSearchRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              add(Pair(SearchProgressItem(), Remove))
              add(Pair(SearchWarningItem_NoResult, AddUpdate))
            }
                .let {
                  searchLiveData.postValue(it)
                }
          }
          dataLoadingLiveData.postValue(false)
        }
  }

  /**
   * Get delegation token for AWS
   */
  fun getDelegationToken(
    awsPath: String,
    file: File
  ) {
    compositeDisposable += userRepository.getDelegationToken(AWSConfig.Target.value())
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            delegationLiveData.postValue(Triple(_res.delegationToken, awsPath, file))
          } else
            error.handle()
        }
  }

}