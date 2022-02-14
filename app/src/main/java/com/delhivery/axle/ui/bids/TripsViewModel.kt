package com.delhivery.axle.ui.bids

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.*
import com.delhivery.axle.api.request.FuelPayoutRequest
import com.delhivery.axle.api.request.OMCRequest
import com.delhivery.axle.api.request.SearchRequest
import com.delhivery.axle.api.response.OMCResponse
import com.delhivery.axle.api.response.TripSummaryResponse
import com.delhivery.axle.data.home.trips.FuelUserSpinnerOptions
import com.delhivery.axle.data.home.trips.PaymentStatus
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
import com.delhivery.axle.ui.bids.ViewPaymentType.AdvancePending
import com.delhivery.axle.ui.bids.ViewPaymentType.BalancePending
import com.delhivery.axle.ui.bids.ViewPaymentType.NA
import com.delhivery.axle.ui.bids.ViewPaymentType.RecoveryPending
import com.delhivery.axle.ui.dialogs.ChangePaymentModeInterface
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
  private val omcRepository: OMCRepository,
  private val transactionsRepository: TransactionsRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel(), FilterTripsInterface, ChangePaymentModeInterface{

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
  var currentTripsCount = -1
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

  var advancePendingTotal = 0.0
  var balancePendingTotal = 0.0
  var recoveryPendingTotal = 0.0

  var fuelCardNumber = ""
  var fuelCardAmt = ""
  var omcID : String = ""
  var tripId: String = ""
  var fuelUserSpinnerOptions = mutableListOf<FuelUserSpinnerOptions>()
  var teamMembersLiveData = MutableLiveData<List<FuelUserSpinnerOptions>>()

  var omcLiveData = MutableLiveData<Triple<String,Int,OMCResponse>>()
  var omcGetLiveData = MutableLiveData<Pair<String,Int>>()
  var fuelPayoutLiveData = MutableLiveData<Triple<String,Int,Pair<Double,String>>>()
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
            advancePendingTotal = _res.advancePending.amount!!
            recoveryPendingTotal = _res.recoveryPending.amount!!
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
      tripsCount = 0
      balancePendingTotal = 0.0
    } else if (paginate && !hasMoreData) {
      return
    }
    currentTripsCount = -1

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
      request.loadedAfter = generateDateString(date, month, year.toString())
    }
//    if (isSettledFilter) {
//      request.settledTrips = true
//    }
    when {
      viewType.equals("all") -> {
        request.tripStatus = statuses
        request.sortBy = "confirmed"
        request.sortDir = "desc"
      }
      viewType.equals("payment_view") -> {
        request.tripStatus = viewPaymentType.status.joinToString(separator = ",") {it}
        request.sortBy = "confirmed"
        request.sortDir = "desc"
      }
      else -> {
        // request.tripStatus = tripType.status.joinToString(separator = ",") { it }
        request.operationTripStatus = tripType.status[0]
        when (tripType) {
          AwaitingArrival -> {
            request.sortBy = "confirmed"
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
          jsonObject.addProperty("limit", UserSearchLimit)
          if (viewType.equals("payment_view") && viewPaymentType == AdvancePending) {
            jsonObject.addProperty("bucket_type", "advance")
          } else if (viewType.equals("payment_view") && viewPaymentType == BalancePending) {
            jsonObject.addProperty("bucket_type", "balance")
          } else if (viewType.equals("payment_view") && viewPaymentType == RecoveryPending) {
            jsonObject.addProperty("bucket_type", "recovery")
          } else if (viewType.equals("all")) {
            jsonObject.addProperty("bucket_type", "all")
          }
//          if (isSettledFilter) {
//            jsonObject.addProperty("bucket_type", "balance_and_recovery")
//          }
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
                currentTripsCount = -1
              }
              /* post all trips with their respective payments as add */
              else {
                currentTripsCount = 0
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
                  if (trip.payment == null) {
//                    total--
                    continue
                  }
                  if ((viewPaymentType == BalancePending && trip.payment!!.status != PaymentStatus.BalancePending.statusKey)
                      || (viewPaymentType == RecoveryPending && trip.payment!!.status != PaymentStatus.RecoveryPending.statusKey)
                      || (viewPaymentType == AdvancePending && trip.payment!!.status != PaymentStatus.AdvancePending.statusKey)) {
//                    total--
                    continue
                  }
                  if (trip.tripStatus == TripStatus.TripCompleted.statusKey && (trip.isApReconPending == true || trip.payment!!.paymentAmount == 0.0)) {
                    trip.isSettled = true
                  }
                  if (isSettledFilter) {
                    if (trip.isSettled) {
                      add(Pair(HomeTripsItem(trip), Add))
                      tripsCount++
                      currentTripsCount++
                    }
                  } else {
                    if (tripsFilter == "issue_trips") {
                      balancePendingTotal += trip.payment!!.paymentAmount ?: 0.0
                    }
                    add(Pair(HomeTripsItem(trip), Add))
                    tripsCount++
                    currentTripsCount++
                  }
                }
              }

              tripsCountText = if (tripsFilter == "issue_trips") {
                "Trips with POD issue (${tripsCount})"
              } else {
                "All Trips (${tripsCount})"
              }
              tripsCountLiveData.postValue(tripsCount)
            }
                .let {
                  userTripsData.postValue(it)
                }
          } else {
            mutableListOf<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              currentTripsCount = -1
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

  fun loadMore(){
    if(currentTripsCount != -1 && currentTripsCount < UserSearchLimit && hasMoreData){
      fetchTrips(true)
    }
  }

  /**
   * generate date string
   */
  private fun generateDateString(day: Int, monthNumber: Int, year: String): String {
    val parser = SimpleDateFormat("yy")
    val formatter = SimpleDateFormat("yyyy")
    var fullYear = formatter.format(parser.parse(year)).toInt()

    val calendar = Calendar.getInstance()
    calendar.set(fullYear, monthNumber, day)

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
      // date = -1
    }
    finalDate = "" + fullYear + "-" + month + "-" + endDay + "T23:59:59"
    return finalDate
  }

  override fun onConfirmClick(filter: String) {
    tripsFilter = filter
    filterAppliedLiveData.postValue(true)
  }

  override fun done(
      transactionId: String,
      omcRequest: OMCRequest,
      omcType: String,
      fuelNumber: String,
      fuelAmt: String,
      position: Int
  ) {
    fuelCardAmt= fuelAmt
    fuelCardNumber = fuelNumber
    tripId = transactionId
    compositeDisposable += omcRepository.omcCard(omcRequest)
        .onBackground()
        .progress()
        .subscribe{ _res ,error ->
          if(!error && _res != null){
            omcLiveData.postValue(Triple(omcType, position, _res))
          }
          else{
            error.handle()
            omcLiveData.postValue(null)
          }
        }
  }

  fun getOMCResult(
    omcType: String,
    position: Int){
    compositeDisposable += userRepository.getOMCs(0, 100, "omc")
        .onBackground()
        .progress()
        .subscribe{ _res, error ->
          if(!error && _res!= null){
            for(item in _res.responseData!!.omcDetailsList){
              if (omcType == item.name){
                omcID = item.uuid
              }
            }
            if(omcID!= "") {
              omcGetLiveData.postValue(Pair(omcType, position))
            }
            else
              omcGetLiveData.postValue(Pair("",position))
          }
          else{
            error.handle()
            omcGetLiveData.postValue(null)
          }
        }
  }

  fun updateTripWithFuelPayout(
    omcType: String,
    pos: Int
  ){
    val fuelPayoutRequest = FuelPayoutRequest("virtual", fuelCardNumber, fuelCardAmt, omcType, omcID, "allocation_update","advance_pending_app")
    compositeDisposable += transactionsRepository.updateTripWithFuelCardUser(tripId, fuelPayoutRequest)
          .onBackground()
          .progress()
          .subscribe(){_res, error ->
            if(!error && _res!= null){
              fuelPayoutLiveData.postValue(Triple(_res.message, pos, Pair(fuelCardAmt.toDouble(),fuelCardNumber)))
            }
            else{
              error.handle()
              fuelPayoutLiveData.postValue(null)
            }

          }

  }

  fun fetchTeamMembers()
  {
    compositeDisposable += userRepository.getUserTeamMembers(0, 100, true, userRepository.userId())
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            fuelUserSpinnerOptions.clear()
            if (_res.total > 0) {
              for (user in _res.users) {
                if (user.phoneNumber != null) {
                  if (user.phoneNumber == userPrefs.phoneNumber)
                  {
                    fuelUserSpinnerOptions.add(FuelUserSpinnerOptions(user.phoneNumber!!, "(Your No.)"))
                  }
                  else if (user.isParent()) {
                    fuelUserSpinnerOptions.add(FuelUserSpinnerOptions(user.phoneNumber!!, "(Admin)"))
                  }
                  else {
                    fuelUserSpinnerOptions.add(FuelUserSpinnerOptions(user.phoneNumber!!, "(Child)"))
                  }
                }
              }
              teamMembersLiveData.postValue(fuelUserSpinnerOptions)
            }
          }
          else{
            teamMembersLiveData.postValue(null)
            error.handle()
          }
        }
  }

}