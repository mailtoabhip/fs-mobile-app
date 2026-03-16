package com.delhivery.axle.ui.tripdetails

import android.text.TextUtils
import android.util.Log
import androidx.core.text.HtmlCompat
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.*
import com.delhivery.axle.api.request.FuelPayoutRequest
import com.delhivery.axle.api.request.OMCRequest
import com.delhivery.axle.api.request.WarehouseRequest
import com.delhivery.axle.api.response.*
import com.delhivery.axle.data.AdvancePaid
import com.delhivery.axle.data.AdvancePending
import com.delhivery.axle.data.AwaitingPODUpload
import com.delhivery.axle.data.AwaitingUnloading
import com.delhivery.axle.data.BalancePaid
import com.delhivery.axle.data.BalancePending
import com.delhivery.axle.data.InTransit
import com.delhivery.axle.data.InTransitLocation
import com.delhivery.axle.data.PODUploaded
import com.delhivery.axle.data.ReachedDestination
import com.delhivery.axle.data.ReachedPickupPoint
import com.delhivery.axle.data.TripHistoryItem
import com.delhivery.axle.data.TripHistoryModel
import com.delhivery.axle.data.TruckLoaded
import com.delhivery.axle.data.TruckPlaced
import com.delhivery.axle.data.TruckUnloaded
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.trips.FuelUserSpinnerOptions
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.TripBidDetails
import com.delhivery.axle.data.home.trips.TripStatus
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryDetailItemData
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryItemData
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryProgressItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.dialogs.ChangePaymentModeInterface
import com.delhivery.axle.ui.team.TeamMemberAdminUserItem
import com.delhivery.axle.ui.team.TeamMemberSubUserItem
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.DatePatterns.OrionDateFormat
import com.delhivery.axle.utils.StringUtils.capitalize
import com.delhivery.axle.utils.extensions.isNotEmpty
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.io.File
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs

/**
 * View model for [TripDetailsActivity]
 */
class TripDetailsViewModel @Inject constructor(
  private val tripsRepository: TripsRepository,
  private val paymentRepository: PaymentRepository,
  private val warehouseRepository: WarehouseRepository,
  private val utilityRepository: UtilityRepository,
  private var userRepository: UserRepository,
  private val payableRepository: PayableRepository,
  private val omcRepository: OMCRepository,
  private val transactionsRepository: TransactionsRepository,
  private val loadboardRepository: LoadboardRepository,
  private val invoiceRepository: InvoiceRepository,
  val userPrefs: UserPrefs
) : BaseViewModel(), ChangePaymentModeInterface {

  /* transaction id */
  lateinit var transactionId: String
  lateinit var tripDetail: HomeTripsItemData
  private var warehouse: String = ""

  /* trip details live data */
  var tripLiveData = MutableLiveData<Pair<HomeBidsRequestItemData, HomeTripsItemData>>()
  var historyLiveData = MutableLiveData<Boolean>()
  var paymentSummaryLiveData = MutableLiveData<Boolean>()
  var warehouseLiveData = MutableLiveData<String>()
  var podDownloadLiveData = MutableLiveData<Pair<String, File>>()
  var invoiceDownloadLiveData = MutableLiveData<String>()

  /* payment summary */
  var chargesSummary = mutableListOf<TripChargesResponse>()
  var paymentsSummary = mutableListOf<TripPaymentsResponse>()

  /* payment summary buckets live data */
  var chargesLiveData = MutableLiveData<Boolean>()
  var paymentLiveData = MutableLiveData<Boolean>()
  var dnRecoveryLiveData = MutableLiveData<Boolean>()
  var overpaymentRecoveryLiveData = MutableLiveData<Boolean>()
  var tripRecoveryLiveData = MutableLiveData<Boolean>()
  var collectionLiveData = MutableLiveData<Boolean>()

  var newPaymentSummary = mutableListOf<PaymentsResponse>()
  var newPaymentTypePayment = mutableListOf<PaymentsResponse>()
  var newPaymentTypeBalance = mutableListOf<PaymentsResponse>()
  var newPaymentTypeDN = mutableListOf<PaymentsResponse>()
  var invoiceList = mutableListOf<String>()
  var chargesListSummary = mutableListOf<ChargesResponse>()

  var chargeDaysList = arrayOf("detention_charge_origin","detention_charge_destination","pod_bonus","pod_penalty","delay")
  var deductionChargesToIgnore = arrayOf("fuel_reimbursement_adj")
  /* trip history */
  var tripHistory = hashMapOf<Int, TripHistoryItem>()

  var tdsRate: Int = 0
  var updatedTDSRate: Double = 0.0

  var balancePaid = false
  var balancePaidTime: String = ""
  var balanceUTR: String = ""
  var advancePaid = false
  var advancePaidTime: String = ""
  var advanceUTR: String = ""
  var bidDetail: TripBidDetails? = null
  var paymentBucketType: String = ""

  var isApReconPending = false
  var tripType: String = ""
  var collections: Double = 0.0
  var payeeId: String = ""
  var totalPendingBalance = 0.0
  var totalPendingRecovery = 0.0
  var totalTDS = 0.0

  var paymentSettled: Boolean = false
  var recoverySettled: Boolean = true
  var settledTime: String ? = ""
  var tripSettledLiveData = MutableLiveData<Boolean>()

  var tripPaymentSummaryLiveData = MutableLiveData<List<Pair<BaseTripPaymentSummaryRVAdapterItem<*>, DataRVAdapterOperationType>>>()
  var chargesSummaryList = mutableListOf<TripPaymentSummaryDetailItemData>()
  var deductionSummaryList = mutableListOf<TripPaymentSummaryDetailItemData>()
  var paymentSummaryList = mutableListOf<TripPaymentSummaryDetailItemData>()
  var recoveriesSummaryList = mutableListOf<TripPaymentSummaryDetailItemData>()
  var pendingRecoveryList = mutableListOf<TripPaymentSummaryDetailItemData>()

  var fuelUserSpinnerOptions = mutableListOf<FuelUserSpinnerOptions>()
  var teamMembersLiveData = MutableLiveData<List<FuelUserSpinnerOptions>>()

  var omcLiveData = MutableLiveData<Pair<String,OMCResponse>>()
  var fuelPayoutLiveData = MutableLiveData<String>()
  var omcGetLiveData = MutableLiveData<Pair<String,String>>()

  var indentLiveData = MutableLiveData<String>()

  var omcID : String = ""
  var fuelCardNumber = ""
  var fuelCardAmt = ""

  companion object{
    var indentList:java.lang.StringBuilder = java.lang.StringBuilder()
  }
  /**
   * Fetch trip details
   */
  fun fetchTripDetails() {
    val parallelTrace = Firebase.performance.newTrace("trips_and_transaction_details_parallel")
    parallelTrace.start()
    compositeDisposable += tripsRepository.tripAndTransactionDetails(transactionId)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if(error != null) parallelTrace.putAttribute("error_response_received", error.message.toString())
          parallelTrace.stop()
          if (!error) {
            this.tripDetail = _res.second
            this.warehouse = _res.first.pickupLocation
            isApReconPending = _res.second.isApReconPending?:false
            
            tripLiveData.postValue(_res)
          } else {
            error.handle()
            tripLiveData.postValue(null)
          }
        }
  }

  /**
   * fetch payment summary of the trip
   */
  fun fetchPayment() {
    val jsonObject = JsonObject()
    jsonObject.addProperty("vendor_id", userRepository.userId())
    jsonObject.addProperty("transaction_ids", transactionId)
    jsonObject.addProperty("offset", 0)
    jsonObject.addProperty("limit", 10)
    if (paymentBucketType.isNotNullOrEmpty()) {
      jsonObject.addProperty("bucket_type", paymentBucketType)
    }
    compositeDisposable += tripsRepository.bulkPayments(listOf(tripDetail), jsonObject)
        .onBackground()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            if (_res.second.isNotEmpty()) {
              tripDetail.payment = _res.second[0]
            }
            paymentSummaryLiveData.postValue(true)
          }
        }
  }

  /**
   * Fetch warehouse details
   */
  fun fetchWarehouseDetails() {
    Pair(TripSummaryProgressItem(TripPaymentSummaryProgressItemData()), DataRVAdapterOperationType.AddUpdate).let { tripPaymentSummaryLiveData.postValue(
        listOf(it)) }

    compositeDisposable += warehouseRepository.fetchWarehouseDetails(
        tripDetail.clientId, warehouse
    )
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            if (_res.warehouses.isNullOrEmpty()) {
              warehouseLiveData.postValue(tripDetail.unloadingLocation)
            } else {
              warehouseLiveData.postValue(_res.warehouses[0].completeAddress())
            }
          }
        }
  }

  /**
   * Fetch history and payments summary
   */
  fun fetchPaymentSummary() {
    compositeDisposable += paymentRepository.historyAndPayments(transactionId)
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            processTrips(_res.first, _res.second)
            historyLiveData.postValue(true)
          } else {
            error.handle()
          }
        }
  }

  /**
   * Fetch Collections summary
   */

  fun fetchCollectionSummary(){
    val jsonObject = JsonObject()
    val jsonElement = JsonPrimitive(transactionId)
    jsonObject.add("trip_id",jsonElement)
    compositeDisposable += payableRepository.fetchCollectionList(jsonObject)
            .onBackground()
            .subscribe{
              _res, error ->
              if(!error){
                collections = 0.0
                if(_res.isNotEmpty()){
                  _res.let{
                    for (collection in _res) {
                      if(collection.type == "waived_off"){
                        collections += collection.amount
                      }
                    }
                    if (collections > 0) {
                      deductionSummaryList.add(
                          TripPaymentSummaryDetailItemData("Waived Off", collections, "", false))
                    }
                  }
                } else{
                  // error?.handle()
                }
              }
              collectionLiveData.postValue(true)
            }
  }

  /**
   * process payment summary
   */
  fun processPaymentSummary() {

    var pendingBalanceRecovery = 0.0
    for (charge in chargesSummaryList) {
      totalPendingBalance += charge.amount
    }
    for (charge in deductionSummaryList) {
      totalPendingBalance -= charge.amount
    }
    for (payment in paymentSummaryList) {
      totalPendingBalance -= payment.amount
    }
    totalPendingBalance += 2*collections
    for (recovery in recoveriesSummaryList) {
      totalPendingBalance += recovery.amount
    }

    var pendingTitle = "Pending Payment"
    if (totalPendingBalance > 0) {
      pendingBalanceRecovery = totalPendingBalance
    } else {
      pendingTitle = "Pending Recovery"
      pendingBalanceRecovery = abs(totalPendingBalance)
    }

    if (pendingRecoveryList.size > 0 || totalPendingBalance < 0.0) {
      recoverySettled = false
    }

    if (totalPendingBalance == 0.0 && tripDetail.tripStatus == "trip_completed") {
      paymentSettled = true
    }

    tripDetail.isSettled = paymentSettled && recoverySettled
    if (tripDetail.isSettled) {
      tripSettledLiveData.postValue(true)
    } else {
      // Clear settled time when trip is not settled
      settledTime = null
      tripSettledLiveData.postValue(false)
    }

    mutableListOf<Pair<BaseTripPaymentSummaryRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(TripSummaryProgressItem(TripPaymentSummaryProgressItemData()), Remove))
      add(Pair(TripSummaryItem(TripPaymentSummaryItemData("Charges", chargesSummaryList, false)), Add))
      add(Pair(TripSummaryItem(TripPaymentSummaryItemData("Deductions", deductionSummaryList, false)), Add))
      add(Pair(TripSummaryItem(TripPaymentSummaryItemData("Payments", paymentSummaryList, false)), Add))
      add(Pair(TripSummaryItem(TripPaymentSummaryItemData(pendingTitle, pendingRecoveryList, false, pendingBalanceRecovery)), Add))
      add(Pair(TripSummaryItem(TripPaymentSummaryItemData("Recoveries Adjusted", recoveriesSummaryList, false)), Add))
    }.let {
      tripPaymentSummaryLiveData.postValue(it)
    }

  }


  /**
   * Fetch Charges List summary
   */
  fun fetchChargeListSummary(){
    val jsonObject = JsonObject()
    val jsonElement = JsonPrimitive(transactionId)
    jsonObject.add("trip_id",jsonElement)
    payeeId = userRepository.userId()
    compositeDisposable += payableRepository.fetchChargesList(jsonObject)
            .onBackground()
            .progress()
            .subscribe{
              _res, error ->
              if(!error){
                chargesListSummary.clear()
                if(_res.isNotEmpty()){
                  _res.let {
                    chargesSummaryList.clear()
                    deductionSummaryList.clear()
                    for (charge in _res){
                      if (charge.payeeId == payeeId){
                        var subtitle = ""
                        if (charge.days > 0) {
                          subtitle = "(" + charge.days.toString() + " days)"
                        }
                        if (charge.action == "deduct") {
                          if(charge.chargeHeadRef in deductionChargesToIgnore){
                            continue
                          }
                          deductionSummaryList.add(TripPaymentSummaryDetailItemData(charge.getChargeTitle(), charge.chargeAmount,
                            subtitle, false
                          ))
                        } else {
                          chargesSummaryList.add(TripPaymentSummaryDetailItemData(charge.getChargeTitle(), charge.chargeAmount,
                            subtitle, false
                          ))
                        }
                      }
                      //chargesListSummary.add(charge)
                      //payeeId = charge.payeeId
                    }
                  }
                }
              } else {
                // error?.handle()
              }
              chargesLiveData.postValue(true)
            }
  }

  /**
   * Fetch Payment summary
   */
  fun fetchNewPaymentSummary(){
    compositeDisposable += paymentRepository.payments(transactionId)
            .onBackground()
            .progress()
            .subscribe{
              _res, error ->
              if(!error){
//                newPaymentSummary.clear()
//                newPaymentTypePayment.clear()
//                newPaymentTypeBalance.clear()
//                newPaymentTypeDN.clear()
                if(_res.isNotEmpty()){
                  _res.let {
                    paymentSummaryList.clear()
                    for (charge in _res){
                      if (charge.status != "success") {
                        continue
                      }
                      // Use payment_at for payment timestamp (not transfer_time)
                      var paymentTime = charge.paymentTimestamp
                      if (paymentTime.isNullOrEmpty()) {
                        val cal = Calendar.getInstance()
                        paymentTime = DateUtils.formatDate(cal.time, OrionDateFormat)
                      }
                      paymentTime.let {
                        val time = DateUtils.formatDate(
                            DateUtils.parseDate(it, OrionDateFormat), DatePatterns.SimpleDateFormat)
                        if (charge.transactionId != transactionId) {
                          if (charge.appliedAmount!! > 0.0) {
                            var subHeading = ""
                            subHeading = if (charge.paymentType == "payment") {
                              "overpayment"
                            } else {
                              charge.dnType ?: ""
                            }
                            deductionSummaryList.add(TripPaymentSummaryDetailItemData("Recovered against: ${capitalize(subHeading)} of ${charge.vehicleNumber} (${time}) (UTR: ${charge.utrNumber})",
                                charge.appliedAmount, "", true, charge.transactionId, VALUE_FUTURE_ADJUSTMENT))
                          } else {

                          }
                        } else if (charge.paymentType == "payment") {
                          var newAmount = charge.amount
                          // val tdsObj = TDS(charge.amount, charge.transferTime)
                          // val tds = tdsObj.getTDS(tdsRate, updatedTDSRate)
                          newAmount -= charge.tdsDeducted
                          totalTDS += charge.tdsDeducted
                          if (charge.head == "balance" && charge.paymentType == "payment" && charge.status == "success") {
                            paymentSettled = true
                            // Use payment_at for settlement timestamp
                            settledTime = paymentTime
                          }
                          var event = capitalize(charge.head)?:""
                          when (charge.head) {
                            "loading" -> {
                              event = "Advance"
                            }
                            "intermittent" -> {
                              event = "In-Transit"
                            }
                            "balance" -> {
                              event = "Balance"
                            }
                            "fuel_reimbursement" -> {
                              event = "Fuel Reimbursement"
                            }
                            "fuel" -> {
                              event = "Fuel Payment to ${charge.vendorName} on Vendor’s Behalf"
                            }
                          }
                          if (charge.amount - charge.tdsDeducted > 0.0) {
                            if (charge.utrNumber.isNotNullOrEmpty()) {
                              paymentSummaryList.add(TripPaymentSummaryDetailItemData(event + " UTR: " + charge.utrNumber!!, newAmount, time, false))
                            } else {
                              paymentSummaryList.add(TripPaymentSummaryDetailItemData(event, newAmount, time, false))
                            }
                          } else {
                            if (charge.utrNumber.isNotNullOrEmpty()) {
                              paymentSummaryList.add(TripPaymentSummaryDetailItemData(event + " UTR: " + charge.utrNumber!!, 0.0, time, false))
                            } else {
                              paymentSummaryList.add(TripPaymentSummaryDetailItemData(event, 0.0, time, false))
                            }
                          }
                        } else {

                        }
                      }
                      //newPaymentSummary.add(charge)
                    }
                    if (totalTDS > 0) {
                      deductionSummaryList.add(
                          TripPaymentSummaryDetailItemData("TDS", totalTDS, "", false)
                      )
                    }
                  }
                }
              } else {
                // error?.handle()
              }
              paymentLiveData.postValue(true)
            }
  }

  /**
   * Fetch List Invoices
   */
  fun fetchListInvoices(){
    compositeDisposable += payableRepository.listInvoices(transactionId)
            .onBackground()
            .subscribe{
              _res, error ->
              if(!error){
                invoiceList.clear()
                if(_res.isNotEmpty()){
                  _res.let {
                    for (invoice in _res){
                      invoiceList.add(invoice.invoiceId)
                    }
                  }
                }
              } else {
                error?.handle()
              }
            }


  }

  /**
   * Fetch DN Recoveries
   */
  fun fetchDNRecoveries(){
    recoveriesSummaryList.clear()
    totalPendingRecovery = 0.0
    totalPendingBalance = 0.0
    val jsonObject = JsonObject()
    val jsonList = JsonArray()
    jsonList.add(transactionId)
    jsonObject.add("trip_ids", jsonList)
    compositeDisposable += payableRepository.listDNRecoveries(jsonObject)
        .onBackground()
        .progress()
        .subscribe {
          _res, error ->
          if (!error && _res != null) {
            if (_res.isNotEmpty()) {
              for (recovery in _res) {
                val recoveryType = capitalize(recovery.dnType)
                recovery.recoveryData?.let {
                  for (data in recovery.recoveryData) {
                    if (data.recoveryTripId != transactionId) {
                      var vehicleNumber = ""
                      var loadedTime = ""
                      vehicleNumber = if (data.recoveryVehicleNumber.isNotNullOrEmpty()) {
                        data.recoveryVehicleNumber ?: ""
                      } else {
                        data.recoveryTripId
                      }
                      if (data.recoveryTripLoadedTime.isNotNullOrEmpty()) {
                        loadedTime = data.recoveryTripLoadedTime?.let {
                          "(Loaded at:" + DateUtils.formatDate(
                            DateUtils.parseDate(it, OrionDateFormat),
                            DatePatterns.SimpleDateFormat
                          ) + ")"
                        } ?: ""
                      }
                      recoveriesSummaryList.add(TripPaymentSummaryDetailItemData("Recovered against: $recoveryType of $vehicleNumber $loadedTime",
                          data.recoveryAmount, "", true, data.recoveryTripId, VALUE_RECOVERY_ADJUSTMENT))
                    }
                  }
                }
              }
            }
          } else {
            // error.handle()
          }
          dnRecoveryLiveData.postValue(true)
        }
  }

  /**
   * Fetch Overpayment Recoveries
   */
  fun fetchOverpaymentRecoveries(){
    val jsonObject = JsonObject()
    val jsonList = JsonArray()
    jsonList.add(transactionId)
    jsonObject.add("trip_ids", jsonList)
    compositeDisposable += payableRepository.listOverpaymentRecoveries(jsonObject)
        .onBackground()
        .progress()
        .subscribe {
          _res, error ->
          if (!error && _res != null) {
            if (_res.responseData != null) {
              for (recovery in _res.responseData) {
                val recoveryType = capitalize(recovery.Type)
                recovery.recoveryData?.let {
                  for (data in recovery.recoveryData) {
                    if (data.recoveryTripId != transactionId) {
                      var vehicleNumber = ""
                      var loadedTime = ""
                      vehicleNumber = if (data.recoveryVehicleNumber.isNotNullOrEmpty()) {
                        data.recoveryVehicleNumber ?: ""
                      } else {
                        data.recoveryTripId
                      }
                      if (data.recoveryTripLoadedTime.isNotNullOrEmpty()) {
                        loadedTime = data.recoveryTripLoadedTime?.let {
                          "(Loaded at:" + DateUtils.formatDate(
                            DateUtils.parseDate(it, OrionDateFormat),
                            DatePatterns.SimpleDateFormat
                          ) + ")"
                        } ?: ""
                      }
                      val utr = data.utrNumber
                      val title = "Recovered against: $recoveryType of $vehicleNumber $loadedTime"
                      if (utr.isNotNullOrEmpty()) {
                        recoveriesSummaryList.add(TripPaymentSummaryDetailItemData(title,
                            data.recoveryAmount, "UTR: " + data.utrNumber, true, data.recoveryTripId, VALUE_RECOVERY_ADJUSTMENT))
                      } else {
                        recoveriesSummaryList.add(TripPaymentSummaryDetailItemData(title,
                            data.recoveryAmount, "", true, data.recoveryTripId, VALUE_RECOVERY_ADJUSTMENT))
                      }
                    }
                  }
                }
              }
            }
          } else {
            // error.handle()
          }
          overpaymentRecoveryLiveData.postValue(true)
        }
  }

  /**
   * Fetch trip recoveries
   */
  fun fetchTripRecoveries() {
    val jsonObject = JsonObject()
    val jsonList = JsonArray()
    jsonList.add(transactionId)
    jsonObject.add("trip_ids", jsonList)
    compositeDisposable += payableRepository.listTripRecoveries(jsonObject)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res.responseData != null) {
            for (recovery in _res.responseData) {
              var subtitle = ""
              if (recovery.type == "overpayment") {
                subtitle = "UTR: " + recovery.utr
              }
              var redirectable = false
              if (recovery.tripId != transactionId) {
                redirectable = true
              }
              deductionSummaryList.add(TripPaymentSummaryDetailItemData(recovery.tripId, recovery.recoveredAmount, subtitle, redirectable))
            }
          }
          tripRecoveryLiveData.postValue(true)
        }
  }

  /**
   * Fetch charges summary
   */
  fun fetchChargeSummary() {
    val jsonObject = JsonObject()
    val jsonArray = JsonArray()
    jsonArray.add(transactionId)
    jsonObject.add("trip_ids", jsonArray)
    compositeDisposable += utilityRepository.fetchCharges(jsonObject)
        .onBackground()
        .subscribe { _res, error ->
          if (!error) {
            chargesSummary.clear()
            if (_res?.values?.isNotEmpty() == true) {
              _res.values.toMutableList()
                  .let {
                    for (charge in it[0].vendorCharges) {
                      chargesSummary.add(charge)
                    }
                  }
            }
          } else {
            error?.handle()
          }
        }
  }

  private fun processTrips(
    histories: List<TripHistoryModel>,
    payments: List<TripPaymentsResponse>
  ) {
    paymentsSummary.clear()
    paymentsSummary.addAll(payments)

    for ((index, history) in histories.withIndex()) {
      when (history.status().statusKey) {
        TripStatus.TruckConfirmed.statusKey -> {
          if (!tripHistory.contains(TruckPlaced)) {
            tripHistory[TruckPlaced] = TripHistoryItem(
                TruckPlaced,
                "Truck Placed",
                "Truck is on its way to pickup location", history.timeStamp()
            )
          }

          if (index == 0 && tripDetail.bidDetails?.advancePayout ?: 0.0 > 0.0
              && !tripHistory.contains(AdvancePending)
          ) {
            tripHistory[AdvancePending] = TripHistoryItem(
                AdvancePending,
                "Advance Pending",
                "₹ ${String.format(
                    "%, .0f",
                    (tripDetail.bidDetails?.advancePayout ?: 0)
                )}" + " will be paid once the loading is completed"
            )
          }
        }

        TripStatus.TruckArrived.statusKey -> {
          if (!tripHistory.contains(ReachedPickupPoint)) {
            tripHistory[ReachedPickupPoint] = TripHistoryItem(
                ReachedPickupPoint,
                "Reached Pickup Point",
                "Driver has reached pickup point",
                history.details?.getArrivalEpoch() ?: ""
            )
          }
        }

        TripStatus.TruckLoaded.statusKey -> {
          if (!tripHistory.contains(TruckLoaded)) {
            tripHistory[TruckLoaded] = TripHistoryItem(
                TruckLoaded,
                "Loading Completed",
                "Truck is ready to start from pickup warehouse",
                history.timeStamp()
            )
          }
        }

        TripStatus.In_Transit.statusKey -> {
          if (TextUtils.isEmpty(history.details?.currentLocation)) {
            if (!tripHistory.contains(InTransit)) {
              tripHistory[InTransit] = TripHistoryItem(
                  InTransit,
                  "In-Transit",
                  "Truck started from pickup location",
                  history.timeStamp()
              )
            }

            if (tripDetail.bidDetails?.advancePayout ?: 0.0 > 0.0) {
              val advancePay = payments.firstOrNull { it.head == "cash_advance" }
              if (advancePay != null) {
                advancePaid = true
                val utrString = when {
                  advancePay.bankTransactionId.isNotNullOrEmpty() -> {
                    advanceUTR = "UTR No: ${advancePay.bankTransactionId}"
                    " with UTR no: ${advancePay.bankTransactionId}."
                  }
                  else -> {
                    ""
                  }
                }

                if (!tripHistory.contains(AdvancePaid)) {
                  tripHistory.remove(AdvancePending)
                  tripHistory[AdvancePaid] = TripHistoryItem(
                      AdvancePaid,
                      "Advance Paid",
                      "Advance payment of ₹${String.format(
                          "%,.0f", (tripDetail.bidDetails?.advancePayout ?: 0)
                      )} has been paid$utrString",
                      history.timeStamp()
                  )
                  advancePaidTime = advancePay.paymentTimestamp?.let {
                    DateUtils.formatDate(
                        DateUtils.parseDate(it, DatePatterns.OrionDateFormat),
                        DatePatterns.SimpleDateFormat
                    )
                  } ?: ""
                }
              } else {
                advancePaid = false
                if (!tripHistory.contains(AdvancePending)) {
                  tripHistory[AdvancePending] = TripHistoryItem(
                      AdvancePending,
                      "Advance Pending",
                      "Advance payment of " +
                          "₹${String.format(
                              "%,.0f", (tripDetail.bidDetails?.advancePayout ?: 0)
                          )}" +
                          " has been initiated"
                  )
                }
              }
            } else {
              advancePaid = false
              if (!tripHistory.contains(AdvancePending)) {
                tripHistory[AdvancePending] = TripHistoryItem(
                    AdvancePending,
                    "Advance Pending",
                    "Advance payment is being processed, will update shortly"
                )
              }
            }
          } else {
            tripHistory[InTransitLocation] = TripHistoryItem(
                InTransitLocation,
                "In-Transit",
                "Truck is in-transit, current location is ${history.details?.currentLocation}",
                history.timeStamp()
            )
          }
        }

        TripStatus.TruckReached.statusKey -> {
          if (index == 0) {
            if (!tripHistory.contains(AwaitingUnloading)) {
              tripHistory[AwaitingUnloading] = TripHistoryItem(
                  AwaitingUnloading,
                  "Awaiting unloading",
                  "Upload ePOD once truck is unloaded",
                  history.details?.getReachedEpoch() ?: ""
              )
            }
          }


          if (!tripHistory.contains(ReachedDestination)) {
            tripHistory[ReachedDestination] = TripHistoryItem(
                ReachedDestination,
                "Reached Destination",
                "Truck reached the destination",
                history.details?.getReachedEpoch() ?: ""
            )
          }
        }

        TripStatus.TruckUnloaded.statusKey -> {
          if (TextUtils.isEmpty(tripDetail.podUrl) &&
              !tripHistory.contains(AwaitingPODUpload)
          ) {
            tripHistory[AwaitingPODUpload] = TripHistoryItem(
                AwaitingPODUpload,
                "Awaiting POD upload",
                "Balance will be paid within 3 days of Physical POD verification"
            )
          } else if (!tripHistory.contains(PODUploaded) && tripDetail.podUrl.isNotNullOrEmpty()) {
            tripHistory[PODUploaded] = TripHistoryItem(
                PODUploaded,
                "POD uploaded",
                "Balance amount will be settled soon",
                history.timeStamp()
            )
          }

          if (!tripHistory.contains(TruckUnloaded)) {
            tripHistory[TruckUnloaded] = TripHistoryItem(
                TruckUnloaded,
                "Truck Unloaded",
                "Trip has been marked complete",
                history.details?.getUnloadedEpoch() ?: ""
            )
          }
        }

        TripStatus.EPodUploaded.statusKey -> {
          if (!tripHistory.contains(PODUploaded)) {
            tripHistory[PODUploaded] = TripHistoryItem(
                PODUploaded,
                "Awaiting Physical POD",
                "Balance will be paid within 3 days of Physical POD verification",
                history.timeStamp()
            )
          }
        }

        TripStatus.TripCompleted.statusKey -> {
          val balancePay = payments.firstOrNull { it.head == "balance_payment" }

          if (balancePay != null) {
            balancePaid = true
            val utrString = when {
              balancePay.bankTransactionId.isNotNullOrEmpty() -> {
                balanceUTR = "UTR no: ${balancePay.bankTransactionId}"
                " with UTR no: ${balancePay.bankTransactionId}."
              }
              else -> {
                ""
              }
            }
            if (!tripHistory.contains(BalancePaid)) {
              tripHistory.remove(BalancePending)
              tripHistory[BalancePaid] = TripHistoryItem(
                  BalancePaid,
                  "Balance Paid",
                  "Balance payment of ₹${String.format(
                      "%,.0f", balancePay.amount
                  )} has been paid$utrString",
                  balancePay.timeStamp()
              )
              balancePaidTime = balancePay.paymentTimestamp?.let { timestamp ->
                DateUtils.formatDate(
                    DateUtils.parseDate(timestamp, DatePatterns.OrionDateFormat),
                    DatePatterns.SimpleDateFormat
                )
              } ?: ""
            }
          } else {
            balancePaid = false
            if (!tripHistory.contains(BalancePending)) {
              tripHistory[BalancePending] = TripHistoryItem(
                  BalancePending,
                  "Balance pending",
                  "Invoice will be shared post payment"
              )
            }
          }

          if (TextUtils.isEmpty(tripDetail.podUrl) &&
              !tripHistory.contains(AwaitingPODUpload)
          ) {
            tripHistory[AwaitingPODUpload] = TripHistoryItem(
                AwaitingPODUpload,
                "Awaiting POD upload",
                "Balance will be paid within 3 days of Physical POD verification"
            )
          } else {
            if (!tripHistory.contains(PODUploaded) && tripDetail.podUrl.isNotNullOrEmpty()) {
              tripHistory[PODUploaded] = TripHistoryItem(
                  PODUploaded,
                  "POD uploaded",
                  "Balance amount will be settled soon",
                  history.timeStamp()
              )
            }
          }
        }
      }
    }
  }

  fun fetchTeamMembers()
  {
    compositeDisposable += loadboardRepository.getUserTeamMembers(userRepository.userId())
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            fuelUserSpinnerOptions.clear()
            if (_res.count > 0) {
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
  /**
   * Prepare download - just pass the URL to the activity
   */
  fun prepareDownload(
    podUrl: String,
    file: File
  ) {
    podDownloadLiveData.postValue(Pair(podUrl, file))
  }

  override fun done(
    transactionId: String,
    omcRequest: OMCRequest,
    omcType: String,
    fuelNumber: String,
    fuelAmt: String,
    position: Int
  ) {
    fuelCardNumber= fuelNumber
    fuelCardAmt = fuelAmt
    compositeDisposable += omcRepository.omcCard(omcRequest)
      .onBackground()
      .progress()
      .subscribe{ _res ,error ->
        if(!error && _res != null){
          omcLiveData.postValue(Pair(omcType,_res))
        }
        else{
          error.handle()
          omcLiveData.postValue(null)
        }
      }
  }

  fun getOMCResult(omcType: String){
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
              omcGetLiveData.postValue(Pair(omcType, omcID))
            }
            else
              omcGetLiveData.postValue(Pair(omcType,""))
          }
          else{
            error.handle()
            omcGetLiveData.postValue(null)
          }
        }
  }

  fun updateTripWithFuelUser(
    omcType:String
  ){
    val fuelPayoutRequest = FuelPayoutRequest("virtual", fuelCardNumber, fuelCardAmt, omcType, omcID, "allocation_update","trip_detail_app")
    compositeDisposable += transactionsRepository.updateTripWithFuelCardUser(transactionId, fuelPayoutRequest)
        .onBackground()
        .progress()
        .subscribe(){_res, error ->
          if(!error && _res!= null){
            fuelPayoutLiveData.postValue(_res.message)
          }
          else{
            error.handle()
            fuelPayoutLiveData.postValue(null)
          }

        }
  }

  fun fetchIndentCenters(code:String) {
    compositeDisposable += warehouseRepository.getWarehouseDetails(WarehouseRequest("facility_code",code, "faas"))
            .onBackground()
            .progress()
            .subscribe { _tRes, error ->
              if (!error) {

                if(indentList.isEmpty()){
                  if (!_tRes.city.isNotNullOrEmpty()) {
                    indentList.append(StringUtils.capitalize(_tRes.city))
                  }
                }else{
                  if (!_tRes.city.isNotNullOrEmpty()) {
                    indentList.append(", ")
                            .append(StringUtils.capitalize(_tRes.city))
                  }
                }
                indentLiveData.postValue(indentList.toString())
              }  else
              {
                error.handle()
              }
            }
  }

  /**
   * Fetch invoice download URL from backend
   */
  fun fetchInvoiceDownloadUrl() {
    compositeDisposable += invoiceRepository.downloadInvoiceDocument(transactionId)
      .onBackground()
      .progress()
      .subscribe { result, error ->
        if (!error && result != null && !result.url.isNullOrEmpty()) {
          invoiceDownloadLiveData.postValue(result.url)
        } else {
          error.handle()
          invoiceDownloadLiveData.postValue(null)
        }
      }
  }

}