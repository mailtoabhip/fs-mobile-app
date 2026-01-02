package com.delhivery.axle.ui.home.fragments.pod

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadCycleRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.repository.UserSearchLimit
import com.delhivery.axle.api.request.SearchRequest
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
 **
 *
 * View model class for [HomePodsFragment]
 *
 **
 */
class HomePodViewModel @Inject constructor(
  private val userRepository: UserRepository,
  private val loadCycleRepository: LoadCycleRepository
) : BaseViewModel() {

  /* user trips live data */
  var userPodsData =
    MutableLiveData<List<Pair<BaseHomePodRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* bids count live data */
  var tripsCountLiveData = MutableLiveData<Int>()
  var dataLoadingLiveData = MutableLiveData<Boolean>()
  val selectedLiveData = MutableLiveData<Int>()
  
  /* pod counts live data */
  var podCountsLiveData = MutableLiveData<PodCounts>()

  var request = SearchRequest()
  var status: TripStatus = TruckUnloaded
  var selectable: Boolean = false
  var dispatch: Boolean = false
  var selectedTransactions = mutableListOf<String>()
  var transactionId: String = ""
  var podUrl: String = ""
  var empty = true

  /* pagination params */
  var total = 0
  var offset = 0
  var hasMoreData = true

  /**
   * Fetch trips
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
    if (status == TruckUnloaded) {
      val cal = Calendar.getInstance()
      cal.add(Calendar.DATE, -14)
      cal.set(Calendar.HOUR_OF_DAY, 0)
      cal.set(Calendar.MINUTE, 0)
      cal.set(Calendar.SECOND, 0)
      DateUtils.formatDate(cal.time, OrionDateFormat)
      request.tripStatus = status.statusKey
      request.value = DateUtils.formatDate(cal.time, OrionDateFormat)
    } else if (status == EPodUploaded) {
      request.tripStatus = EPodUploaded.statusKey + "," + TruckUnloaded.statusKey
      request.value = null
    }
    compositeDisposable += loadCycleRepository.searchTrips(request.getRequest())
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            offset += _res.trips.size
            hasMoreData = _res.hasNext
            total = _res.total
            
            // Update pod counts
            _res.podCounts?.let { podCountsLiveData.postValue(it) }

            mutableListOf<Pair<BaseHomePodRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomePodProgressItem(), Remove))

              /* empty view, if fresh fetch n total == 0 */
              if (!paginate && total == 0) {
                add(Pair(HomePodWarningItem_NoLoads, AddUpdate))
              }
              /* post all transactions mapped to bids as add */
              else {
              //  add(Pair(HomePodSearchItem(), AddUpdate))
                for (trip in _res.trips) {
                  when (status) {
                    TruckUnloaded -> {
                      if (!trip.hasPODTracking()) {
                        empty = false
                        trip.selectable = selectable
                        add(Pair(HomePodTripItem(trip), Add))
                      }
                    }
                    else -> {
                      if (dispatch) {
                        if (trip.hasPODTracking()) {
                          empty = false
                          add(Pair(HomePodTripItem(trip), Add))
                        }
                      } else {
                        if (!trip.hasPODTracking()) {
                          empty = false
                          add(Pair(HomePodTripItem(trip), Add))
                        }
                      }
                    }
                  }
                }
                if (empty)
                  add(Pair(HomePodWarningItem_NoLoads, AddUpdate))
              }
            }
                .let { userPodsData.postValue(it) }
          } else {
            mutableListOf<Pair<BaseHomePodRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* add api time out item */
              add(Pair(HomePodWarningItem_TimeOut, AddUpdate))
            }
                .let { userPodsData.postValue(it) }
          }

          dataLoadingLiveData.postValue(false)
        }
  }

}