package com.delhivery.axle.ui.home.activity.fuel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.data.fuelcards.FuelCardData
import com.delhivery.axle.repository.FuelRepository
import com.delhivery.axle.repository.TripsRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.bids.TripType
import com.delhivery.axle.ui.bids.TripType.InTransit
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class TripsFuelCardViewModel @Inject constructor(
  private val tripsRepository: TripsRepository,
  private val fuelRepository: FuelRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  private lateinit var cards: List<FuelCardData>

  var hasMoreData = true
  var offset = 0
  var total = 0
  var trip: TripType = InTransit

  /* user trips live data */
  var tripsliveData =
    MutableLiveData<List<Pair<BaseTripsFuelRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  fun fetchTrips(paginate: Boolean) {
    if (!paginate) {
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }

    compositeDisposable += tripsRepository.trips(
        offset, trip.status.joinToString(separator = ",") { it }
    )
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            offset += _res.trips.size
            hasMoreData = _res.hasNext
            total = _res.total

            mutableListOf<Pair<BaseTripsFuelRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              add(Pair(TripsFuelProgressItem(), Remove))
              if (total == 0) {
              } else {
                val trips = _res.trips
                for (trip in trips) {
                  try {
                    val cardList = cards.filter { p ->
                      p.tripId.safeEquals(trip.transactionId)
                    }
                    if (cardList.isNullOrEmpty()) {
                      trip.fuelCard = FuelCardData("","","0","","")
                    } else {
                      trip.fuelCard = cardList[0]
                    }
                  } catch (e: Exception) {
                    Log.d("No payment found for: ", trip.transactionId)
                  }
                  add(Pair(TripsFuelDataItem(trip), Add))
                }
              }
            }
                .let {
                  tripsliveData.postValue(it)
                }
          } else {
            mutableListOf<Pair<BaseTripsFuelRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {

            }
                .let {
                  tripsliveData.postValue(it)
                }
          }
        }
  }

  fun fetchFuelCards() {
    compositeDisposable += fuelRepository.fetchActiveFuelCards()
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            this.cards = _res.cards
            fetchTrips(false)
          } else {

          }
        }
  }
}