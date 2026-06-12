package com.dfd.delfin.ui.home.activity.fuel

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.FuelRepository
import com.dfd.delfin.api.repository.LoadCycleRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.api.repository.UserSearchLimit
import com.dfd.delfin.api.request.SearchRequest
import com.dfd.delfin.data.fuelcards.FuelCardData
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.Add
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.dfd.delfin.utils.DateUtils
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import javax.inject.Inject

/**
 * Viewmodel for [ActiveTripsActivity]
 */
class ActiveTripsViewModel @Inject constructor(
  private val fuelRepository: FuelRepository,
  private val loadCycleRepository: LoadCycleRepository,
  private val userRepository: UserRepository
) : BaseViewModel() {

  private lateinit var cards: List<FuelCardData>

  var hasMoreData = true
  var offset = 0
  var total = 0
  // var trip: TripType = ActiveForFuel
  var optinDate = ""
  var request = SearchRequest()

  var tripsLiveData =
    MutableLiveData<List<Pair<BaseActiveTripsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* data loading live data */
  var dataLoadingLiveData = MutableLiveData<Boolean>()

  /**
   * Fetches all active trips
   */
  fun fetchTrips(paginate: Boolean) {
    if (!paginate) {
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }

    if (paginate) {
      Pair(ActiveTripProgressItem(), AddUpdate).let { tripsLiveData.postValue(listOf(it)) }
    }

    dataLoadingLiveData.postValue(true)

    request.offset = offset
    request.limit = UserSearchLimit
    request.vendorId = userRepository.userId()
    // request.tripStatus = trip.status.joinToString(separator = ",") { it }
    request.updatedAfter = DateUtils.formatISODateToUTC(optinDate, "YYYY-MM-dd'T'HH:mm:ss")
  }

  /**
   * Fetches all active fuel cards
   */
  fun fetchFuelCards() {
    compositeDisposable += fuelRepository.fetchActiveFuelCards()
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            this.cards = _res.cards
            fetchTrips(false)
          } else {
            mutableListOf<Pair<BaseActiveTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              add(Pair(ActiveTripProgressItem(), Remove))
              add(Pair(ActiveTripWarningItem_TimeOut, Add))
            }
                .let {
                  tripsLiveData.postValue(it)
                }
          }
        }
  }

  /**
   * Fetches all numbers with active fuel cards
   */
  fun getActiveNumbers(mobile: String?): ArrayList<String> {
    val numbers = arrayListOf<String>()
    for (card in cards) {
      if (card.mobile.compareTo(mobile ?: "") != 0)
        numbers.add(card.mobile)
    }
    return numbers
  }
}