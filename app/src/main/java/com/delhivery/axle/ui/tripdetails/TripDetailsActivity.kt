package com.delhivery.axle.ui.tripdetails

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.response.*
import com.delhivery.axle.api.response.TripPaymentsResponse.ChargeType
import com.delhivery.axle.data.AwaitingPODUpload
import com.delhivery.axle.data.BalancePaid
import com.delhivery.axle.data.PODUploaded
import com.delhivery.axle.data.TripHistoryItem
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.HomeTripsTimeOutAction
import com.delhivery.axle.data.home.trips.TripStatus
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryDetailItemAction
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryDetailItemData
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryItemAction
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryItemData
import com.delhivery.axle.databinding.ActivityTripDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.ChangePaymentModeDialog
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.AWSUtils.AWSProgressInterface
import com.delhivery.axle.utils.EVENT_PAYMENT_SUMMARY
import com.delhivery.axle.utils.EVENT_POD_VIEWED
import com.delhivery.axle.utils.EVENT_TRIP_STATUS_HISTORY
import com.delhivery.axle.utils.PROPERTY_STATUS
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_ID
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_TYPE
import com.delhivery.axle.utils.REQCODE_STORAGE
import com.delhivery.axle.utils.REQCODE_UPLOAD_POD
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.VALUE_FAILURE
import com.delhivery.axle.utils.VALUE_LOAD
import com.delhivery.axle.utils.VALUE_SUCCESS
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.io.File
import javax.inject.Inject

/**
 * Trip detail screen
 */
class TripDetailsActivity : BaseActivity<ActivityTripDetailsBinding, TripDetailsViewModel>(),
    AWSProgressInterface, TripPaymentSummaryRVAdapterInterface {

  init {
    hasInlineProgress = true
  }

  override fun getViewModelClass() = TripDetailsViewModel::class.java

  override fun layoutId() = R.layout.activity_trip_details

  override fun requireConnection() = true

  @Inject lateinit var awsUtils: AWSUtils
  @Inject lateinit var userPrefs: UserPrefs

  private val adapter: TripPaymentSummaryRVAdapter by lazy { TripPaymentSummaryRVAdapter(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    /* validate intent */
    try {
      require(
          !(intent == null || !intent.hasExtra(TransactionIdIntentKey))
      ) { "Required data $TransactionIdIntentKey not found" }
    } catch (e: Exception) {
      finish()
    }

    /* set transaction id */
    viewModel.transactionId = intent?.getStringExtra(TransactionIdIntentKey) ?: ""
    viewModel.tripType = intent?.getStringExtra(IntentExtraTripTypeKey)?: ""

    onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        userPrefs.setPreviousScreen(this.javaClass.name)
        finish()
      }
    })

  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    updateTDSRate()

    /* observe trip details live data */
    viewModel.progressLiveData.observe(this, ProgressObserver())
    viewModel.tripLiveData.observe(this, TransactionObserver())
    viewModel.paymentSummaryLiveData.observe(this, Observer {
      binding.tripDetails = viewModel.tripDetail
      binding.actionChangePaymentMode.isEnabled = true
    })

    viewModel.warehouseLiveData.observe(this, Observer {
      // binding.labelWarehouse.text = it
    })
    viewModel.tripSettledLiveData.observe(this, Observer {
      binding.tripSettled = it
      binding.viewModel = viewModel
    })
    viewModel.delegationLiveData.observe(this, Observer {
      if (it != null) {
        awsUtils.startDownload(it.first, it.second, it.third, this)
      } else {
        uiUtils.showSnackbar("Please try again")
      }
    })

    binding.rvPmtSummary.visibility = View.GONE
    binding.rvPmtSummary.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
      adapter = this@TripDetailsActivity.adapter
    }

    viewModel.tripPaymentSummaryLiveData.observe(this, Observer {
      binding.refreshLayout.isRefreshing = false
      it?.let { _items ->
        adapter.operation(_items)
      }
    })

    viewModel.teamMembersLiveData.observe(this , Observer {
      uiUtils.hideProgress()
      if(it!= null){
        ChangePaymentModeDialog(this,viewModel, viewModel.tripDetail, it, userPrefs, uiUtils, analyticsUtil = analyticsUtil).show()
        analyticsUtil.trackEvent(
          EVENT_VIEW_CHANGE_PAYMENT_MODE_TRIP_DETAILS,
          mutableListOf(PROPERTY_USER_ID),
          mutableListOf(userPrefs.userId())
        )
      }
    })

    viewModel.omcLiveData.observe(this, Observer{
      uiUtils.hideProgress()
      if(it != null){
        uiUtils.showProgress()
        viewModel.getOMCResult(it.first)
      }
    })

    viewModel.omcGetLiveData.observe(this, Observer {
      uiUtils.hideProgress()
      if(it != null){
         if(it.second != "") {
           uiUtils.showProgress()
           viewModel.updateTripWithFuelUser(it.first)
         }
        else{
          uiUtils.showSnackbar("OMC not found")
         }
      }
    })
    viewModel.fuelPayoutLiveData.observe(this, Observer {
      uiUtils.hideProgress()
      if(it!=null){
        uiUtils.showSnackbar(it)
        refreshData()
      }
    })

    binding.containerError.btnAction.setOnClickListener {
      refreshData()
    }

    binding.refreshLayout.setOnRefreshListener {
      refreshData()
    }

    binding.llPickupDestination.setOnClickListener {
      viewModel.tripDetail.addressExpand = !viewModel.tripDetail.addressExpand
      binding.tripDetails = viewModel.tripDetail
    }

    binding.callDriverText.setOnClickListener {
      compositeDisposable += requestPermission(arrayOf(Manifest.permission.CALL_PHONE))
          .onBackground()
          .subscribe { granted, error ->
            if (error == null && granted) {
              when (viewModel.tripDetail.driverDetails?.driverPhoneNo?.let { it1 ->
                contactUtils.callDriver(
                    it1
                )
              }) {
                false -> {
                  uiUtils.showSnackbar("Unable to place call")
                }
                else -> {
                }
              }
            } else {
              uiUtils.showSnackbar(getString(string.msg_call_permission))
            }
          }
    }

    binding.actionChangePaymentMode.setOnClickListener {
      uiUtils.showProgress()
      viewModel.fetchTeamMembers()

    }

    viewModel.chargesLiveData.observe(this, Observer {
      viewModel.fetchNewPaymentSummary()
    })

    viewModel.paymentLiveData.observe(this, Observer {
      viewModel.fetchDNRecoveries()
    })

    viewModel.dnRecoveryLiveData.observe(this, Observer {
      viewModel.fetchOverpaymentRecoveries()
    })

    viewModel.overpaymentRecoveryLiveData.observe(this, Observer {
      viewModel.fetchTripRecoveries()
    })

    viewModel.tripRecoveryLiveData.observe(this, Observer {
      viewModel.fetchCollectionSummary()
    })

    viewModel.collectionLiveData.observe(this, Observer {
      viewModel.processPaymentSummary()
    })

    viewModel.indentLiveData.observe(this, Observer {
      if(it.isNotNullOrEmpty()){
          runOnUiThread {
            binding.textViaLabel.visibility = View.VISIBLE
            binding.textViaDestination.visibility = View.VISIBLE
          binding.textViaDestination.text = it
        }
      }
    })

    refreshData()
  }

  override fun onDestroy() {
    super.onDestroy()
  }

  private fun refreshData() {
      binding.refreshing = true
      binding.error = false
      adapter.resetStaticData()
      viewModel.totalTDS = 0.0
      viewModel.tripHistory.clear()
      viewModel.chargesSummary.clear()
      viewModel.paymentsSummary.clear()
      viewModel.chargesListSummary.clear()
      viewModel.newPaymentSummary.clear()
      viewModel.newPaymentTypePayment.clear()
      viewModel.newPaymentTypeBalance.clear()
      viewModel.newPaymentTypeDN.clear()
      viewModel.fetchTripDetails()
  }

  /**
   * Transaction details and UI updation Observer
   */
  inner class TransactionObserver : Observer<Pair<HomeBidsRequestItemData, HomeTripsItemData>> {
    override fun onChanged(t: Pair<HomeBidsRequestItemData, HomeTripsItemData>?) {
      binding.refreshing = false
      if (t != null) {
        binding.error = false
        title = t.first.tripDisplayName()
        binding.transaction = t.first
        binding.tripDetails = t.second
        viewModel.bidDetail = t.second.bidDetails
        if ((viewModel.tripDetail.tripStatus != "truck_confirmed") &&
            (viewModel.tripDetail.tripStatus != "truck_arrived")) {
          binding.rvPmtSummary.visibility = View.VISIBLE
          viewModel.paymentBucketType = "all"
        }
        if(t.second.entity!="OSCPL"){
          viewModel.fetchWarehouseDetails()
        }

        if(binding.transaction?.indentOrigin.equals("LH")){
          if(!binding.transaction?.indentHaltCenters.isNullOrEmpty()){
            for(code in binding.transaction?.indentHaltCenters!!){
              viewModel.fetchIndentCenters(code.haltCenterCode)
            }
          }
        }else{
           val stopBuilder = StringBuilder()

          if (!TextUtils.isEmpty(binding.transaction?.pickup1City)) {
            binding.textViaLabel.visibility = View.VISIBLE
            binding.textViaDestination.visibility = View.VISIBLE
            stopBuilder.append(StringUtils.capitalize(binding.transaction?.pickup1City))
          }
          if (!TextUtils.isEmpty(binding.transaction?.pickup2City)) {
            binding.textViaLabel.visibility = View.VISIBLE
            binding.textViaDestination.visibility = View.VISIBLE
            stopBuilder.append(", ").append(StringUtils.capitalize(binding.transaction?.pickup2City))
          }
          if (!TextUtils.isEmpty(binding.transaction?.stop1City)) {
            binding.textViaLabel.visibility = View.VISIBLE
            binding.textViaDestination.visibility = View.VISIBLE
            stopBuilder.append(", ").append(StringUtils.capitalize(binding.transaction?.stop1City))
          }
          if (!TextUtils.isEmpty(binding.transaction?.stop2City)) {
            binding.textViaLabel.visibility = View.VISIBLE
            binding.textViaDestination.visibility = View.VISIBLE
            stopBuilder.append(", ").append(StringUtils.capitalize(binding.transaction?.stop2City))
          }

          binding.textViaDestination.text = stopBuilder.toString()
        }

        viewModel.fetchPayment()
        viewModel.fetchChargeListSummary()


        // viewModel.fetchPaymentSummary()
        // viewModel.fetchChargeSummary()
        // viewModel.fetchListInvoices()
      } else {
        binding.error = true
        binding.containerError.title = "Session Time Out"
        binding.containerError.subTitle =
          "Unfortunately, we couldn't fetch the data you are looking for. Kindly refresh."
        binding.containerError.actionLabel = "REFRESH"
      }
      binding.executePendingBindings()
    }
  }

  /**
   * Progress observer
   */
  inner class ProgressObserver : Observer<Boolean> {
    override fun onChanged(t: Boolean?) {
      t?.let {
        when (t) {
          true -> {
            binding.refreshLayout.isRefreshing = true
          }
          false -> {
            binding.refreshLayout.isRefreshing = false
          }
        }
        binding.executePendingBindings()
      }
    }
  }

  private fun requestStoragePermission() {
    val storagePermission =
      ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    if (storagePermission != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(
          this,
          arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
          REQCODE_STORAGE
      )
    } else {
      uiUtils.showSnackbar("Downloading POD....")
      downloadFile()
    }
  }

  override fun onAWSSuccess(
    path: String
  ) {
    if (!isFinishing) {
      uiUtils.hideProgress()
      uiUtils.showSnackbar("Downloaded")
      val file = getFile()
      if (file != null) {
        analyticsUtil.trackEvent(
            EVENT_POD_VIEWED,
            mutableListOf(PROPERTY_STATUS),
            mutableListOf(VALUE_SUCCESS)
        )
        openFile(file)
      } else {
        uiUtils.showSnackbar("Can't process POD")
      }
    }
  }

  override fun onAWSFailure() {
    if (!isFinishing) {
      analyticsUtil.trackEvent(
          EVENT_POD_VIEWED,
          mutableListOf(PROPERTY_STATUS),
          mutableListOf(VALUE_FAILURE)
      )
      uiUtils.hideProgress()
      uiUtils.showSnackbar("Couldn't complete download, please try after sometime")
    }
  }

  private fun downloadFile() {
    val file = getFile()
    if (file != null && !TextUtils.isEmpty(viewModel.tripDetail.podUrl)) {
      uiUtils.showProgress()
      viewModel.tripDetail.podUrl?.let { viewModel.getDelegationToken(it, file) }
    } else {
      uiUtils.showSnackbar("Can't process POD")
    }
  }

  private fun getFile(): File? {
    val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val basePath = "$storageDir/" + viewModel.transactionId
    val awsPath = viewModel.tripDetail.podUrl
    if (awsPath != null) {
      return when {
        (awsPath).endsWith("pdf") -> File(basePath + "_pod.pdf")
        (awsPath).endsWith("png") -> File(basePath + "_pod.png")
        (awsPath).endsWith("jpg") || (awsPath).endsWith("jpeg") -> File(basePath + "_pod.jpg")
        else -> {
          return null
        }
      }
    }
    return null
  }

  private fun openFile(file: File) {
    try {
      val uri =
        FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
      val intent = Intent(Intent.ACTION_VIEW)
      if (file.toString()
              .contains(".pdf")
      ) {
        intent.setDataAndType(uri, "application/pdf")
      } else if (file.toString()
              .contains(".jpg") ||
          file.toString()
              .contains(".jpeg") || file.toString()
              .contains(".png")
      ) {
        intent.setDataAndType(uri, "image/jpeg")
      } else {
        intent.setDataAndType(uri, "*/*")
      }
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      startActivity(intent)
    } catch (e: java.lang.Exception) {
      uiUtils.showSnackbar("No application found which can open the file")
    }
  }

  private fun String.capitalizeWords(): String = split(" ").map { StringUtils.capitalize(it) }.joinToString(" ")

  private fun redirectToLRsTrip(transactionId: String){
    startActivity(tripDetailsIntent(transactionId, this, viewModel.tripType))
  }

  private fun updateTDSRate(){
    if (viewModel.userPrefs.userType == "individual") {
      viewModel.tdsRate = 1
      viewModel.updatedTDSRate = 0.75
    } else {
      viewModel.tdsRate = 2
      viewModel.updatedTDSRate = 1.5
    }
  }

  private fun getDeductionHead(dnType: String, vehicleNumber: String, loadedTime: String, utr: String): SpannableString{
    val head = "Recovery against $dnType\n$vehicleNumber (${getPaymentDate(loadedTime)}) \n(UTR: $utr)"
    var i = 0
    for(char in head){
      if(char == '\n'){
        break
      }
      i += 1
    }
    i += 1
    val spannableHead = SpannableString(head)
    spannableHead.setSpan(UnderlineSpan(), i, i+vehicleNumber!!.length, 0)
    spannableHead.setSpan(ForegroundColorSpan(ContextCompat.getColor(
            this@TripDetailsActivity,
            R.color.link
    )), i, i+vehicleNumber!!.length, 0)
    return spannableHead
  }

  private fun getPaymentHead(head : String, utr: String, date: String): String{
    var newHead = head.replace("_"," ").capitalizeWords()
    if(newHead == "Intermittent"){
      newHead = "In-transit"
    } else if(newHead == "Loading"){
      newHead = "Advance"
    }
    return "$newHead UTR: $utr, \n${getPaymentDate(date)}"
  }

  private fun getPaymentDate(transferTime: String): String{
    var year = transferTime.substring(2,4)
    var day = transferTime.substring(8,10)
    if(day.get(0) == '0'){
      day = transferTime.substring(9,10)
    }
    var month = transferTime.substring(5,7)
    if(month.get(0) == '0'){
      month = transferTime.substring(6,7)
    }
    if (month != null) {
      month = DateUtils.getMonth(month.toInt())
    }
    return "$day $month $year"
  }

  private fun getPaymentAmount(amount: Double, date: String): Double{
    var newAmount = amount
    val tdsObj = TDS(amount, date)
    val tds = tdsObj?.getTDS(viewModel.tdsRate, viewModel.updatedTDSRate)
    if (tds != null) {
      newAmount -= tds
    }
    return newAmount
  }

  private fun getChargeText(chargeHead: String, days: Int): String{
    var chargeText: String
    var chargeDays: Int
    when (chargeHead) {
      "detention_charge_origin" -> {
        chargeText = "Loading Detention"
      }
      "detention_charge_destination" -> {
        chargeText = "Unloading Detention"
      }
      else -> {
        chargeText = chargeHead.replace('_',' ').capitalizeWords()
      }
    }
    if(chargeHead in viewModel.chargeDaysList){
      chargeDays = days
      if(chargeDays == 1){
        chargeText += " (1 day)"
      }else if(chargeDays != 0 && chargeDays > 1){
        chargeText += " ($chargeDays days)"
      }
    }
    return chargeText
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQCODE_STORAGE) {
      if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
        uiUtils.showSnackbar("Storage permission required to download POD")
      } else {
        requestStoragePermission()
      }
    }
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQCODE_UPLOAD_POD && resultCode == RESULT_OK) {
      refreshData()
    }
  }

  override fun handleAction(actionId: String, position: Int, item: BaseTripPaymentSummaryRVAdapterItem<*>) {
    when (actionId) {
      TripPaymentSummaryItemAction -> {
        val data = item.data as TripPaymentSummaryItemData
        if(!data.expanded){
          when(data.title.uppercase()){
            "CHARGES" -> {
              analyticsUtil.trackEvent(
                      EVENT_VIEW_CHARGES,
                      mutableListOf(PROPERTY_USER_ID, PROPERTY_TRANSACTION_ID, PROPERTY_CHARGES_AMOUNT, PROPERTY_AMOUNT_TYPE),
                      mutableListOf(userPrefs.userId() , viewModel.transactionId, data.totalAmount() , VALUE_PENDING_AMOUNT)
              )
            }
            "DEDUCTIONS" -> {
              analyticsUtil.trackEvent(
                      EVENT_VIEW_DEDUCTIONS,
                      mutableListOf(PROPERTY_USER_ID, PROPERTY_TRANSACTION_ID, PROPERTY_DEDUCTIONS_AMOUNT, PROPERTY_AMOUNT_TYPE),
                      mutableListOf(userPrefs.userId() , viewModel.transactionId, data.totalAmount() , VALUE_RECOVERY_AMOUNT)
              )
            }
            "PAYMENTS" -> {
              analyticsUtil.trackEvent(
                      EVENT_VIEW_PAYMENTS,
                      mutableListOf(PROPERTY_USER_ID, PROPERTY_TRANSACTION_ID, PROPERTY_PAYMENTS_AMOUNT, PROPERTY_AMOUNT_TYPE),
                      mutableListOf(userPrefs.userId() , viewModel.transactionId, data.totalAmount() , VALUE_PENDING_AMOUNT)
              )
            }
            "RECOVERIES ADJUSTED" -> {
              analyticsUtil.trackEvent(
                      EVENT_VIEW_RECOVERIES_ADJUSTED,
                      mutableListOf(PROPERTY_USER_ID, PROPERTY_TRANSACTION_ID, PROPERTY_RECOVERIES_ADJUSTED_AMOUNT, PROPERTY_AMOUNT_TYPE),
                      mutableListOf(userPrefs.userId() , viewModel.transactionId, data.totalAmount() , VALUE_RECOVERY_AMOUNT)
              )
            }
          }
        }
        adapter.toggle(position, data)
      }

      HomeTripsTimeOutAction -> {
        refreshData()
      }

      TripPaymentSummaryDetailItemAction -> {
        val data = item.data as TripPaymentSummaryDetailItemData
        if (data.redirectable == true) {
          data.transactionId?.let {
            //Capture Event
            var event :String ?= null
            if (data.eventType == VALUE_RECOVERY_ADJUSTMENT){
              event = EVENT_VIEW_TRIP_RECOVERY_ADJUSTMENT
            }
            else if(data.eventType  == VALUE_FUTURE_ADJUSTMENT){
              event = EVENT_VIEW_TRIP_FUTURE_ADJUSTMENT
            }
            if (event != null) {
              analyticsUtil.trackEvent(
                        event,
                        mutableListOf(PROPERTY_USER_ID , PROPERTY_TRANSACTION_ID , PROPERTY_TRIP_AGAINST_RECOVERY_ADJUSTED , PROPERTY_AMOUNT_OF_RECOVERY_ADJUSTED ),
                        mutableListOf(userPrefs.userId() , viewModel.transactionId, data.transactionId ?: "" , data.formattedAmount() )
                )
            }
            userPrefs.setPreviousScreen(this.javaClass.name)
            startActivity(tripDetailsIntent(data.transactionId!!, this))
          }
        }
      }
    }
  }

  /*override fun onBackPressed() {
    super.onBackPressed()
    userPrefs.setPreviousScreen(this.javaClass.name)
  }*/
}

/* intent keys */
private const val TransactionIdIntentKey = "transaction_id"
private const val IntentExtraTripTypeKey = "trip_type"

/**
 * Trip details intent
 */
fun tripDetailsIntent(
  transactionId: String,
  context: Context,
  tripType: String = ""
) = Intent(context, TripDetailsActivity::class.java).apply {
  putExtra(TransactionIdIntentKey, transactionId)
  putExtra(IntentExtraTripTypeKey, tripType)
}

