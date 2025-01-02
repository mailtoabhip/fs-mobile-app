package com.delhivery.axle.ui.home.fragments.trips

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.ExpenseRepository
import com.delhivery.axle.api.repository.LoadCycleRepository
import com.delhivery.axle.api.repository.PayableRepository
import com.delhivery.axle.api.repository.TripsRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.SearchRequest
import com.delhivery.axle.api.response.DownloadLedgerResponse
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.dialogs.DownloadLedgerInterface
import com.delhivery.axle.ui.ledger.BaseConsolidatedPageRVAdapterItem
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * View model class for [HomeTripsFragment]
 *
 **
 */
class HomeTripsViewModel @Inject constructor(
  private val tripsRepository: TripsRepository,
  private val loadCycleRepository: LoadCycleRepository,
  private val userRepository: UserRepository,
  private val expenseRepository: ExpenseRepository,
  private var payableRepository: PayableRepository
) : BaseViewModel(), DownloadLedgerInterface {

  var awaitingArrivalCount = ""
  var inTransitCount = ""
  var awaitingPodCount = ""
  var awaitingLoadingCount = ""
  var awaitingUnloadingCount = ""
  var advancePendingCount = ""
  var advancePendingAmount = ""
  var balancePendingCount = ""
  var balancePendingAmount = ""
  var recoveryPendingCount = ""
  var recoveryPendingAmount = ""
  var totalOngoingCount = 0

  var ledgerLiveData = MutableLiveData<List<Pair<BaseConsolidatedPageRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  var dataLoadingLiveData = MutableLiveData<Boolean>()

  var emailLoadingLiveData = MutableLiveData<String>()

  var downloadLoadingLiveData = MutableLiveData<DownloadLedgerResponse>()

  var downloadPressed = MutableLiveData<Boolean>()

  var currentStartMonth = -1
  var currentStartYear = -1

  var currentEndMonth = -1
  var currentEndYear = -1

  var ledgerStartDate = -1
  var ledgerEndDate = -1

  var hasMoreData: Boolean = false
  var offset:Int? = 0
  var total = 0

  /* user trips live data */
  var userTripsData =
    MutableLiveData<List<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* bids count live data */
  var tripsCountLiveData = MutableLiveData<Int>()

  var request = SearchRequest()

  /**
   * Fetch trips summary
   */
  fun fetchTripsSummary() {
    compositeDisposable += tripsRepository.userTripsSummary()
        .onBackground()
        .subscribe { _res, error ->
          if (!error && _res != null) {

            advancePendingCount = _res.advancePending.count()
            advancePendingAmount = "₹ ${StringUtils.formatAmount(_res.advancePending.amount ?: 0.0)}"
            balancePendingCount = _res.balancePending.count()
            balancePendingAmount = "₹ ${StringUtils.formatAmount(_res.balancePending.amount ?: 0.0)}"
            recoveryPendingCount = _res.recoveryPending.count()
            recoveryPendingAmount = "₹ ${StringUtils.formatAmount(_res.recoveryPending.amount ?: 0.0)}"
            awaitingArrivalCount = _res.awaitingArrival.count()
            inTransitCount = _res.inTransit.count()
            awaitingPodCount = _res.awaitingPod.count()
            awaitingLoadingCount = _res.awaitingLoading.count()
            awaitingUnloadingCount = _res.awaitingUnloading.count()
            totalOngoingCount = (_res.awaitingArrival.count ?: 0) + (_res.inTransit.count ?: 0) + (_res.awaitingPod.count ?: 0) + (_res.awaitingLoading.count ?: 0) + (_res.awaitingUnloading.count ?: 0)

            dataLoadingLiveData.postValue(true)
          } else {
            error.handle()
          }
        }
  }

  @SuppressLint("SimpleDateFormat")
  fun generateDateString(type: String, monthNumber: Int, year: String, recent: Boolean=false): String {
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
    if (type == "startDate") {
      if(recent){
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val today = format.format(Date())
        val todayFormatted: Date = format.parse(today)
        val fifteenDaysFormatted = Date(todayFormatted.getTime() - 1296000000L) // 15 * 24 * 60 * 60 * 1000
        val fifteenDays = format.format(fifteenDaysFormatted.time)

        startDay = fifteenDays.substring(8,10)
        month = fifteenDays.substring(5,7)
        fullYear = fifteenDays.substring(0,4).toInt()
      }
      if(ledgerStartDate != -1){
        startDay = ledgerStartDate.toString()
        if(startDay.length == 1){
          startDay = "0$startDay"
        }
        ledgerStartDate = -1
      }
      finalDate = "" + fullYear + "-" + month + "-" + startDay + "T00:00:00"
    } else if (type == "endDate") {
      if(ledgerEndDate != -1){
        endDay = ledgerEndDate.toString()
        if(endDay.length == 1){
          endDay = "0$endDay"
        }
        ledgerEndDate = -1
      }
      finalDate = "" + fullYear + "-" + month + "-" + endDay + "T23:59:59"
    }
    return finalDate
  }

  private fun generatePayloadDownloadEmailLedger(startDate: String, endDate: String, optionFilter: String = "all", email: String = ""): JsonObject {
    val root = JsonObject()
    val rangeFilterArray = JsonArray()
    val startObject = JsonObject()
    val endObject = JsonObject()

    startObject.add("column", JsonPrimitive("status_update_info.truck_loaded.at"))
    startObject.add("value", JsonPrimitive(startDate))
    startObject.add("operator", JsonPrimitive("gte"))

    endObject.add("column", JsonPrimitive("status_update_info.truck_loaded.at"))
    endObject.add("value", JsonPrimitive(endDate))
    endObject.add("operator", JsonPrimitive("lte"))

    rangeFilterArray.add(startObject)
    rangeFilterArray.add(endObject)

    root.add("payee_id", JsonPrimitive(userRepository.userId()))
    root.add("range_filters", rangeFilterArray)

    if (optionFilter.isNotNullOrEmpty()) {
      if (optionFilter == "filter_trips_with_pending_payments"
          || optionFilter == "filter_pending_recovery_trips"
          || optionFilter == "filter_settled_trips") {
        root.addProperty(optionFilter, true)
      }
    }

    if(email != "") {
      root.add("email_id", JsonPrimitive(email))
    }

    return root
  }

  private fun downloadVendorLedger(jsonObject: JsonObject){
    compositeDisposable += payableRepository.downloadVendorLedger(jsonObject)
        .onBackground()
        .progress()
        .subscribe{
          _res,error ->
          if(!error){
            Log.d("Download response",""+_res)
            downloadLoadingLiveData.postValue(_res)
          } else {
            error.handle()
          }
        }
  }

  private fun emailVendorLedger(jsonObject: JsonObject){
    compositeDisposable += payableRepository.emailVendorLedger(jsonObject)
        .onBackground()
        .progress()
        .subscribe{
          _res,error ->
          if(!error){
            Log.d("Email response",""+_res)
            emailLoadingLiveData.postValue(_res.message)
          } else {
            error.handle()
          }
        }

  }

  fun initiateDownloadAndEmail(type: String, startMonth:Int, startYear:Int,endMonth:Int, endYear:Int, optionFilter: String = "" ,email: String = ""){
    val startRange = generateDateString("startDate",startMonth,startYear.toString())
    val endRange = generateDateString("endDate",endMonth,endYear.toString())

    if (type == "email" && email != ""){
      val jsonObject = generatePayloadDownloadEmailLedger(startRange,endRange, optionFilter, email)
      emailVendorLedger(jsonObject)
    }else if(type == "download"){
      val jsonObject = generatePayloadDownloadEmailLedger(startRange,endRange, optionFilter)
      downloadVendorLedger(jsonObject)
    }
  }

  override fun onEmailClick(startDate: Int, startMonth: Int, startYear: Int, endDate: Int, endMonth: Int, endYear: Int, optionFilter: String ,email: String) {
    Log.d("Email->","$startDate-$startMonth-$startYear---->$email")
    ledgerStartDate = startDate
    ledgerEndDate = endDate
    initiateDownloadAndEmail("email", startMonth,startYear, endMonth, endYear, optionFilter, email)
  }

  override fun onDownloadClick(startDate: Int, startMonth: Int, startYear: Int, endDate: Int, endMonth: Int, endYear: Int, optionFilter: String) {
    Log.d("Download->","$endDate-$endMonth-$endYear")
    ledgerStartDate = startDate
    ledgerEndDate = endDate
    downloadPressed.postValue(true)
    initiateDownloadAndEmail("download", startMonth,startYear, endMonth, endYear, optionFilter)
  }
}