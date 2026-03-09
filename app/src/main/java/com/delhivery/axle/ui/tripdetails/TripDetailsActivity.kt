package com.delhivery.axle.ui.tripdetails

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.response.*
import com.delhivery.axle.api.response.FileData
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.bids.SUB_REQUEST_TYPE_INTRACITY
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.HomeTripsTimeOutAction
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryDetailItemAction
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryDetailItemData
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryItemAction
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryItemData
import com.delhivery.axle.databinding.ActivityTripDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.ChangePaymentModeDialog
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.DocumentUtils
import com.delhivery.axle.utils.EVENT_POD_VIEWED
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import com.delhivery.axle.utils.PROPERTY_STATUS
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_ID
import com.delhivery.axle.utils.REQCODE_UPLOAD_POD
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.VALUE_FAILURE
import com.delhivery.axle.utils.VALUE_SUCCESS
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File
import javax.inject.Inject

/**
 * Trip detail screen
 */
class TripDetailsActivity : BaseActivity<ActivityTripDetailsBinding, TripDetailsViewModel>(),
    DocumentUtils.DocumentProgressInterface, DocumentUtils.DocumentListInterface, TripPaymentSummaryRVAdapterInterface {

  init {
    hasInlineProgress = true
  }

  override fun getViewModelClass() = TripDetailsViewModel::class.java

  override fun layoutId() = R.layout.activity_trip_details

  override fun requireConnection() = true

  @Inject lateinit var documentUtils: DocumentUtils
  @Inject lateinit var userPrefs: UserPrefs
  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true

  private val adapter: TripPaymentSummaryRVAdapter by lazy { TripPaymentSummaryRVAdapter(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("TripDetailsActivity_SetupTime")
    activitySetupTrace?.start()
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
    binding.backArrow.setOnClickListener {
      onBackPressedDispatcher.onBackPressed()
    }
      binding.clAdhocintracity.buttonAbc.setOnClickListener {
          refreshData()
      }
      
      binding.clAdhocintracity.buttonDownloadInvoice.setOnClickListener {
          downloadInvoice()
      }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)


    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }

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
    viewModel.podDownloadLiveData.observe(this, Observer {
      if (it != null) {
        downloadPOD(it.first, it.second)
      } else {
        uiUtils.showSnackbar("Please try again")
      }
    })

    viewModel.invoiceDownloadLiveData.observe(this, Observer { downloadUrl ->
      if (downloadUrl != null && currentInvoiceFile != null) {
        downloadFileFromUrl(downloadUrl, currentInvoiceFile!!)
      } else {
        uiUtils.hideProgress()
        resetDownloadContext()
      }
    })

    viewModel.invoiceErrorLiveData.observe(this, Observer { errorMessage ->
      if (errorMessage != null) {
        uiUtils.showSnackbar(errorMessage)
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
        analyticsUtil.moEngageTrackEvent(
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
       callDriver()
    }

    binding.clAdhocintracity.callDriver.setOnClickListener {
      callDriver()
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

  override fun onResume() {
    super.onResume()
    if (activitySetupTrace != null && isFirstResume) {
      activitySetupTrace?.stop()
      isFirstResume = false
    }
  }

   fun callDriver(){
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
   * Update settled milestone with payment timestamp
   */
  private fun updateSettledMilestone() {
    val trip = binding.tripDetails ?: return
    
    // Check if trip is settled
    if (!trip.isSettled) {
        binding.clAdhocintracity.tvSettledTimestamp?.visibility = View.GONE
        return
    }
    
    // Get payment data
    val payment = trip.payment
    if (payment == null) {
        binding.clAdhocintracity.tvSettledTimestamp?.visibility = View.GONE
        return
    }
    
    val timestamp = payment.paymentTimestamp
    val status = payment.paymentStatus
    val failureMessage = payment.paymentFailureMessage
    
    when {
        // Success with timestamp
        status == "success" && !timestamp.isNullOrEmpty() -> {
            try {
                val date = DateUtils.parseDate(timestamp, DatePatterns.OrionDateFormat)
                val sdf = java.text.SimpleDateFormat("dd-MMM-yyyy hh:mma", java.util.Locale.ENGLISH)
                val formattedText = "Payment made at ${sdf.format(date)}"
                
                binding.clAdhocintracity.tvSettledTimestamp.text = formattedText
                binding.clAdhocintracity.tvSettledTimestamp.setTextColor(
                    ContextCompat.getColor(this, R.color.sub_heading_black)
                )
                binding.clAdhocintracity.tvSettledTimestamp.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.clAdhocintracity.tvSettledTimestamp.visibility = View.GONE
            }
        }
        
        // Failed with message
        status == "failed" && !failureMessage.isNullOrEmpty() -> {
            binding.clAdhocintracity.tvSettledTimestamp.text = failureMessage
            binding.clAdhocintracity.tvSettledTimestamp.setTextColor(
                ContextCompat.getColor(this, R.color.colorDelhiveryRed)
            )
            binding.clAdhocintracity.tvSettledTimestamp.visibility = View.VISIBLE
        }
        
        // Failed without message
        status == "failed" -> {
            binding.clAdhocintracity.tvSettledTimestamp.text = "Payment failed - contact support"
            binding.clAdhocintracity.tvSettledTimestamp.setTextColor(
                ContextCompat.getColor(this, R.color.colorDelhiveryRed)
            )
            binding.clAdhocintracity.tvSettledTimestamp.visibility = View.VISIBLE
        }
        
        // No timestamp available
        else -> {
            binding.clAdhocintracity.tvSettledTimestamp.visibility = View.GONE
        }
    }
  }

  /**
   * Transaction details and UI updation Observer
   */
  inner class TransactionObserver : Observer<Pair<HomeBidsRequestItemData, HomeTripsItemData>?> {
    override fun onChanged(t: Pair<HomeBidsRequestItemData, HomeTripsItemData>?) {
      binding.refreshing = false
      if (t != null) {
        binding.error = false
        if (t.first.subRequestType == SUB_REQUEST_TYPE_INTRACITY) {
         binding.toolbar.visibility = View.GONE
          binding.intracityTitle.visibility = View.VISIBLE
          binding.llTripDetails.visibility = View.GONE
          binding.clAdhocintracity.root.visibility = View.VISIBLE
          binding.clAdhocintracity.tripDetails = t.second
          binding.clAdhocintracity.request = t.first
          binding.clAdhocintracity.layoutTransaction.request = t.first
          
          // Update settled milestone timestamp
          updateSettledMilestone()

        } else {
          binding.toolbar.visibility = View.VISIBLE
          binding.intracityTitle.visibility = View.GONE
          binding.clAdhocintracity.root.visibility = View.GONE
          binding.llTripDetails.visibility = View.VISIBLE
        title = t.first.tripDisplayName()
        }
        binding.transaction = t.first
        binding.tripDetails = t.second
        viewModel.bidDetail = t.second.bidDetails

        if ((viewModel.tripDetail.tripStatus != "truck_confirmed") &&
          (viewModel.tripDetail.tripStatus != "truck_arrived")
        ) {
          binding.rvPmtSummary.visibility = View.VISIBLE
          viewModel.paymentBucketType = "all"
        }
        if (t.second.entity != "OSCPL") {
          viewModel.fetchWarehouseDetails()
        }

        if (binding.transaction?.indentOrigin.equals("LH")) {
          if (!binding.transaction?.indentHaltCenters.isNullOrEmpty()) {
            for (code in binding.transaction?.indentHaltCenters!!) {
              viewModel.fetchIndentCenters(code.haltCenterCode)
            }
          }
        } else {
          val stopBuilder = StringBuilder()

          if (!TextUtils.isEmpty(binding.transaction?.pickup1City)) {
            binding.textViaLabel.visibility = View.VISIBLE
            binding.textViaDestination.visibility = View.VISIBLE
            stopBuilder.append(StringUtils.capitalize(binding.transaction?.pickup1City))
          }
          if (!TextUtils.isEmpty(binding.transaction?.pickup2City)) {
            binding.textViaLabel.visibility = View.VISIBLE
            binding.textViaDestination.visibility = View.VISIBLE
            stopBuilder.append(", ")
              .append(StringUtils.capitalize(binding.transaction?.pickup2City))
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
  inner class ProgressObserver : Observer<Boolean?> {
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

  /**
   * Download POD file using scoped storage (no permission needed)
   */
  private fun downloadPODFile() {
    uiUtils.showSnackbar("Downloading POD....")
    downloadFile()
  }

  override fun onDocumentSuccess(downloadUrl: String) {
    // TripDetailsActivity doesn't use upload functionality
    // This method is required by DocumentProgressInterface but not used here
  }

  override fun onDocumentFailure(error: String) {
    // TripDetailsActivity doesn't use upload functionality
    // This method is required by DocumentProgressInterface but not used here
  }

//  private fun downloadFileDirectly(url: String, file: File) {
//    // Download file from URL using standard HTTP/OkHttp
//    uiUtils.showProgress()
//    compositeDisposable += io.reactivex.Observable.fromCallable {
//      val request = okhttp3.Request.Builder().url(url).build()
//      val response = okhttp3.OkHttpClient().newCall(request).execute()
//      if (response.isSuccessful) {
//        response.body?.byteStream()?.use { input ->
//          file.outputStream().use { output ->
//            input.copyTo(output)
//          }
//        }
//        true
//      } else {
//        false
//      }
//    }
//    .onBackground()
//    .subscribe { success, error ->
//      uiUtils.hideProgress()
//      if (success && error == null) {
//        analyticsUtil.moEngageTrackEvent(
//            EVENT_POD_VIEWED,
//            mutableListOf(PROPERTY_STATUS),
//            mutableListOf(VALUE_SUCCESS)
//        )
//        uiUtils.showSnackbar("Downloaded")
//        openFile(file)
//      } else {
//        analyticsUtil.moEngageTrackEvent(
//            EVENT_POD_VIEWED,
//            mutableListOf(PROPERTY_STATUS),
//            mutableListOf(VALUE_FAILURE)
//        )
//        uiUtils.showSnackbar("Couldn't complete download, please try after sometime")
//      }
//    }
//  }

  private fun downloadFile() {
    val file = getFile()
    if (file != null && !TextUtils.isEmpty(viewModel.tripDetail.podUrl)) {
      viewModel.tripDetail.podUrl?.let { podUrl ->
        downloadPOD(podUrl, file)
      }
    } else {
      uiUtils.showSnackbar("Can't process POD")
    }
  }

  /**
   * Download POD using secure Document API
   */
  private fun downloadPOD(podUrl: String, file: File) {
    uiUtils.showProgress()
    // Extract s3_path from full S3 URL
    val s3Path = extractS3PathFromUrl(podUrl)
    if (s3Path != null) {
      // Store mapping for callback
      currentPodUrl = podUrl
      currentPodFile = file
      // Use secure Document API to download
      documentUtils.downloadByS3Path(s3Path, this)
    } else {
      uiUtils.hideProgress()
      uiUtils.showSnackbar("Unable to extract document path")
    }
  }

  /**
   * Extract s3_path from full S3 URL
   */
  private fun extractS3PathFromUrl(docUrl: String): String? {
    return try {
      // Check if it's already a relative path (no http/https protocol and no .amazonaws.com)
      if (!docUrl.startsWith("http://") && !docUrl.startsWith("https://") && !docUrl.contains(".amazonaws.com")) {
        // Already a relative path, return as-is (remove query parameters if any)
        return docUrl.split("?")[0]
      }
      
      // Construct AWS base path from config
      val bucket = com.delhivery.axle.config.AWSConfig.Bucket.value()
      val region = com.delhivery.axle.config.AWSConfig.ServerRegion.value()
      val awsBasePath = "https://$bucket.s3.$region.amazonaws.com/"
      
      // Remove base URL from full S3 URL
      if (docUrl.startsWith(awsBasePath)) {
        docUrl.replace(awsBasePath, "").split("?")[0] // Remove query parameters if any
      } else {
        // Fallback: Try to extract path from URL pattern (bucket.s3.region.amazonaws.com/path)
        val parts = docUrl.split(".amazonaws.com/")
        if (parts.size > 1) {
          parts[1].split("?")[0] // Remove query parameters if any
        } else {
          null
        }
      }
    } catch (e: Exception) {
      Log.e("TripDetailsActivity", "Error extracting s3_path: ${e.message}")
      null
    }
  }

  // Store current download context
  private var currentPodUrl: String? = null
  private var currentPodFile: File? = null
  private var currentInvoiceFile: File? = null

  // DocumentListInterface implementation for secure download API
  override fun onDocumentListSuccess(files: List<FileData>) {
    if (files.isNotEmpty()) {
      val documentFile = files.first()
      val downloadUrl = documentFile.downloadUrl
      
      if (currentPodFile != null) {
        // POD download
        downloadFileFromUrl(downloadUrl, currentPodFile!!)
      } else {
        uiUtils.hideProgress()
        uiUtils.showSnackbar("Download context lost")
      }
    } else {
      uiUtils.hideProgress()
      uiUtils.showSnackbar("No documents found")
      resetDownloadContext()
    }
  }

  override fun onDocumentListFailure(error: String) {
    uiUtils.hideProgress()
    uiUtils.showSnackbar("Download failed: $error")
    resetDownloadContext()
  }

  /**
   * Reset all download contexts
   */
  private fun resetDownloadContext() {
    currentPodUrl = null
    currentPodFile = null
    currentInvoiceFile = null
  }

  /**
   * Download invoice using backend API (returns pre-signed URL directly)
   */
  private fun downloadInvoice() {
    uiUtils.showProgress()
    
    // Prepare file for invoice
    currentInvoiceFile = getInvoiceFile()
    
    if (currentInvoiceFile == null) {
      uiUtils.hideProgress()
      uiUtils.showSnackbar("Unable to prepare invoice file")
      return
    }
    
    // Call backend API to get pre-signed URL
    viewModel.fetchInvoiceDownloadUrl()
  }

  /**
   * Get file for invoice download
   */
  private fun getInvoiceFile(): File? {
    val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val basePath = "$storageDir/${viewModel.transactionId}_invoice.pdf"
    return File(basePath)
  }

  /**
   * Download file from pre-signed URL using OkHttpClient
   */
  private fun downloadFileFromUrl(downloadUrl: String, file: File) {
    compositeDisposable += io.reactivex.Observable.fromCallable {
      val client = OkHttpClient()
      val request = Request.Builder()
        .url(downloadUrl)
        .build()
      
      val response: Response = client.newCall(request).execute()
      
      if (response.isSuccessful) {
        response.body()?.byteStream()?.use { input ->
          file.outputStream().use { output ->
            input.copyTo(output)
          }
        }
        file.absolutePath
      } else {
        null
      }
    }
    .onBackground()
    .subscribe({ filePath ->
      if (filePath != null) {
        openFile(file)
      } else {
        uiUtils.showSnackbar("Download failed")
      }
      uiUtils.hideProgress()
      resetDownloadContext()
    }, { error ->
      uiUtils.hideProgress()
      uiUtils.showSnackbar("Download error: ${error.message}")
      resetDownloadContext()
    })
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
              analyticsUtil.moEngageTrackEvent(
                      EVENT_VIEW_CHARGES,
                      mutableListOf(PROPERTY_USER_ID, PROPERTY_TRANSACTION_ID, PROPERTY_CHARGES_AMOUNT, PROPERTY_AMOUNT_TYPE),
                      mutableListOf(userPrefs.userId() , viewModel.transactionId, data.totalAmount() , VALUE_PENDING_AMOUNT)
              )
            }
            "DEDUCTIONS" -> {
              analyticsUtil.moEngageTrackEvent(
                      EVENT_VIEW_DEDUCTIONS,
                      mutableListOf(PROPERTY_USER_ID, PROPERTY_TRANSACTION_ID, PROPERTY_DEDUCTIONS_AMOUNT, PROPERTY_AMOUNT_TYPE),
                      mutableListOf(userPrefs.userId() , viewModel.transactionId, data.totalAmount() , VALUE_RECOVERY_AMOUNT)
              )
            }
            "PAYMENTS" -> {
              analyticsUtil.moEngageTrackEvent(
                      EVENT_VIEW_PAYMENTS,
                      mutableListOf(PROPERTY_USER_ID, PROPERTY_TRANSACTION_ID, PROPERTY_PAYMENTS_AMOUNT, PROPERTY_AMOUNT_TYPE),
                      mutableListOf(userPrefs.userId() , viewModel.transactionId, data.totalAmount() , VALUE_PENDING_AMOUNT)
              )
            }
            "RECOVERIES ADJUSTED" -> {
              analyticsUtil.moEngageTrackEvent(
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
              analyticsUtil.moEngageTrackEvent(
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

