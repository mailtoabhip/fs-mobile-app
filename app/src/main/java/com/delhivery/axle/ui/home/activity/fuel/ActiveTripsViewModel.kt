package com.delhivery.axle.ui.home.activity.fuel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.FuelRepository
import com.delhivery.axle.api.repository.LoadCycleRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.repository.UserSearchLimit
import com.delhivery.axle.api.request.SearchRequest
import com.delhivery.axle.data.fuelcards.FuelCardData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
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
    compositeDisposable += loadCycleRepository.searchTrips(request.getRequest())
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            offset += _res.trips.size
            hasMoreData = _res.hasNext
            total = _res.total

            mutableListOf<Pair<BaseActiveTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              add(Pair(ActiveTripProgressItem(), Remove))
              if (total == 0) {
                add(Pair(ActiveTripWarningItem_NoTrip, Add))
              } else {
                val trips = _res.trips
                for (trip in trips) {
                  try {
                    val cardList = cards.filter { p ->
                      p.tripId.safeEquals(trip.transactionId)
                    }
                    if (cardList.isNullOrEmpty()) {
                      trip.fuelCard = FuelCardData("", "", "0", "", "")
                    } else {
                      trip.fuelCard = cardList[0]
                    }
                  } catch (e: Exception) {
                    Log.d("No payment found for: ", trip.transactionId)
                  }
                  add(Pair(ActiveTripFuelDataItem(trip), Add))
                }
              }
            }
                .let {
                  tripsLiveData.postValue(it)
                }
          } else {
            mutableListOf<Pair<BaseActiveTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              add(Pair(ActiveTripProgressItem(), Remove))
              add(Pair(ActiveTripWarningItem_TimeOut, Add))
            }
                .let {
                  tripsLiveData.postValue(it)
                }
          }
          dataLoadingLiveData.postValue(false)
        }
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