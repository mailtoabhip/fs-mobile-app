package com.delhivery.axle.ui.bids

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.ExpenseRepository
import com.delhivery.axle.api.repository.LoadCycleRepository
import com.delhivery.axle.api.repository.TripsRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.repository.UserSearchLimit
import com.delhivery.axle.api.request.SearchRequest
import com.delhivery.axle.api.response.TripSummaryResponse
import com.delhivery.axle.data.home.trips.TripStatus
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.bids.TripType.AwaitingArrival
import com.delhivery.axle.ui.bids.TripType.AwaitingLoading
import com.delhivery.axle.ui.bids.TripType.AwaitingUnloading
import com.delhivery.axle.ui.bids.TripType.InTransit
import com.delhivery.axle.ui.bids.TripType.Unknown
import com.delhivery.axle.ui.bids.ViewPaymentType.BalancePending
import com.delhivery.axle.ui.bids.ViewPaymentType.NA
import com.delhivery.axle.ui.bids.ViewPaymentType.RecoveryPending
import com.delhivery.axle.ui.dialogs.FilterTripsInterface
import com.delhivery.axle.ui.home.fragments.trips.BaseHomeTripsRVAdapterItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsProgressItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsWarningItem_NoTrips
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsWarningItem_TimeOut
import com.delhivery.axle.utils.DatePatterns
import com.delhivery.axle.utils.DatePatterns.OrionDateFormat
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeEquals
import com.delhivery.axle.utils.extensions.toDate
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonObject
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * View model class for [TripsActivity]
 *
 **
 */
class TripsViewModel @Inject constructor(
  private val expenseRepository: ExpenseRepository,
  private val tripsRepository: TripsRepository,
  private val loadCycleRepository: LoadCycleRepository,
  private val userRepository: UserRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel(), FilterTripsInterface {

  /* user trips live data */
  var userTripsData =
    MutableLiveData<List<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* trips count live data */
  var tripsCountLiveData = MutableLiveData<Int>()

  /* data loading live data */
  var dataLoadingLiveData = MutableLiveData<Boolean>()

  var summaryLiveData = MutableLiveData<TripSummaryResponse>()

  var filterAppliedLiveData = MutableLiveData<Boolean>()

  /* pagination params */
  var hasMoreData = true
  var offset = 0

  var request = SearchRequest()
  var tripType: TripType = Unknown
  var viewPaymentType: ViewPaymentType = NA
  var viewType: String ?= "all"
  var total = 0
  var issueTripsCount = 0
  var tripsCount = 0
  var tripsFilter = ""
  var tripsCountText = ""
  var filterList: List<String> = listOf()
  var filterKey: String = ""
  var loadingDateFilter: Boolean = false
  var loadingDate: String = ""
  var isSettledFilter: Boolean = false

  var date = -1
  var month = -1
  var year = -1

  /**
   * Fetch trips summary
   */
  fun fetchTripsSummary() {
    compositeDisposable += tripsRepository.userTripsSummary()
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            issueTripsCount = _res.issueTrips ?: 0
            summaryLiveData.postValue(_res)
          } else {
            error.handle()
          }
        }
  }

  /**
   * Fetch user trips
   */
  fun fetchTrips(paginate: Boolean) {
    if (!paginate) {
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }

    if (paginate) {
      showProgress()
      /* add progress if not paginating */
      Pair(HomeTripsProgressItem(), AddUpdate).let { userTripsData.postValue(listOf(it)) }
    }

    dataLoadingLiveData.postValue(true)

    val statuses = mutableListOf<String>().apply {
      add(TripStatus.In_Transit.statusKey)
      add(TripStatus.TruckArrived.statusKey)
      add(TripStatus.TruckConfirmed.statusKey)
      add(TripStatus.TruckLoaded.statusKey)
      add(TripStatus.TruckReached.statusKey)
      add(TripStatus.TruckUnloaded.statusKey)
      add(TripStatus.EPodUploaded.statusKey)
      add(TripStatus.TripCompleted.statusKey)
    }
        .joinToString(separator = ",") { it }

    request.offset = offset
    request.limit = UserSearchLimit
    request.vendorId = userRepository.userId()
    when (tripsFilter) {
      "issue_trips" -> {
        request.issueTrips = true
      }
      "less_than_1_day", "1_day", "2_days", "more_than_3_days" -> {
        if (filterKey == "arrived_ageing") {
          request.arrivedAgeing = tripsFilter
        } else {
          request.reachedAgeing = tripsFilter
        }
      }
      "delayed" -> {
        request.delayed = true
      }
      else -> {

      }
    }
    if (loadingDateFilter) {
      request.loadedAfter = generateDateString(month, year.toString())
    }
//    if (isSettledFilter) {
//      request.settledTrips = true
//    }
    when {
      viewType.equals("all") -> {
        request.tripStatus = statuses
      }
      viewType.equals("payment_view") -> {
        request.tripStatus = viewPaymentType.status.joinToString(separator = ",") {it}
      }
      else -> {
        // request.tripStatus = tripType.status.joinToString(separator = ",") { it }
        request.operationTripStatus = tripType.status[0]
        when (tripType) {
          AwaitingArrival -> {
            request.sortBy = "required_on"
            request.sortDir = "asc"
          }
          InTransit -> {
            request.sortBy = "loaded"
            request.sortDir = "desc"
          }
          AwaitingLoading -> {
            request.sortBy = "arrival"
            request.sortDir = "desc"
          }
          AwaitingUnloading -> {
            request.sortBy = "reached"
            request.sortDir = "desc"
          }
          else -> {
            request.sortBy = "loaded"
            request.sortDir = "desc"
          }
        }
      }
    }
    compositeDisposable += loadCycleRepository.searchTrips(request.getRequest())
        .flatMap { t ->
          offset += t.trips.size
          hasMoreData = t.hasNext
          total = t.total

          val jsonObject = JsonObject()
          jsonObject.addProperty("vendor_id", userRepository.userId())
          jsonObject.addProperty("transaction_ids", t.trips.map { it.transactionId }.joinToString(",") { it })
          jsonObject.addProperty("offset", 0)
          jsonObject.addProperty("limit", 10)
          if (viewType.equals("payment_view") && viewPaymentType == BalancePending) {
            jsonObject.addProperty("bucket_type", "balance")
          } else if (viewType.equals("payment_view") && viewPaymentType == RecoveryPending) {
            jsonObject.addProperty("bucket_type", "recovery")
          }
          if (isSettledFilter) {
            jsonObject.addProperty("bucket_type", "balance_and_recovery")
          }
          tripsRepository.bulkPayments(t.trips, jsonObject)
        }
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            mutableListOf<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeTripsProgressItem(), Remove))
              val trips = _res.first
              val payments = _res.second

              /* No trips found, if fresh fetch n total == 0 */
              if (total == 0) {
                add(Pair(HomeTripsWarningItem_NoTrips, AddUpdate))
              }
              /* post all trips with their respective payments as add */
              else {
                for (trip in trips) {
                  trip.tds = userPrefs.tdsRate
                  trip.updatedTds = userPrefs.updatedTdsRate
                  try {
                    if (trip.tripStatus == TripStatus.In_Transit.statusKey) {
                      val currentTime = Calendar.getInstance()
                      val promiseDate = trip.promiseDate?.let { it } ?: ""
                      if (DateUtils.parseDate(promiseDate, OrionDateFormat).time < currentTime.timeInMillis) {
                        trip.isDelayed = true
                      }
                    }
                  } catch (e: Exception) {
                    Log.d("No PD found for: ", trip.transactionId)
                  }
                  try {
                    trip.payment = payments.filter { p ->
                      p.transactionId.safeEquals(trip.transactionId)
                    }[0]

                  } catch (e: Exception) {
                    Log.d("No payment found for: ", trip.transactionId)
                  }
                  if ((viewPaymentType == BalancePending && trip.payment!!.status !="balance_pending")
                      || (viewPaymentType == RecoveryPending && trip.payment!!.status !="recovery_pending")) {
                    total--
                    continue
                  }
                  if (isSettledFilter) {
                    if (trip.tripStatus != "trip_completed") {
                      total--
                      continue
                    }
                    if (trip.isApReconPending != true) {
                      if (trip.payment!!.paymentAmount != 0.0 ) {
                        total--
                        continue
                      }
                    }
                    trip.isSettled = true
                    tripsCount++
                  }
                  add(Pair(HomeTripsItem(trip), Add))
                }
              }
              tripsCountText = if (tripsFilter == "issue_trips") {
                "Trips with the issue (${total})"
              } else {
                "All Trips (${total})"
              }
              tripsCountLiveData.postValue(total)
            }
                .let {
                  userTripsData.postValue(it)
                }
          } else {
            mutableListOf<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              /* remove progress item */
              add(Pair(HomeTripsProgressItem(), Remove))
              /* add api time out item */
              add(Pair(HomeTripsWarningItem_TimeOut, AddUpdate))
            }
                .let { userTripsData.postValue(it) }
          }

          dataLoadingLiveData.postValue(false)
        }
  }

  /**
   * generate date string
   */
  private fun generateDateString(monthNumber: Int, year: String): String {
    val parser = SimpleDateFormat("yy")
    val formatter = SimpleDateFormat("yyyy")
    var fullYear = formatter.format(parser.parse(year)).toInt()

    val calendar = Calendar.getInstance()
    calendar.set(fullYear, monthNumber, 1)

    var endDay = calendar.getActualMaximum(Calendar.DATE).toString()
    var startDay = "01"

    var month = "" + (monthNumber + 1)
    if (month.length == 1) {
      month = "0$month"
    }

    var finalDate = ""
    if (date != -1) {
      endDay = date.toString()
      if(endDay.length == 1){
        endDay = "0$endDay"
      }
      date = -1
    }
    finalDate = "" + fullYear + "-" + month + "-" + endDay + "T23:59:59"
    return finalDate
  }

  override fun onConfirmClick(filter: String) {
    tripsFilter = filter
    filterAppliedLiveData.postValue(true)
  }

}