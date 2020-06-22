package com.delhivery.axle.ui.tripdetails

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.api.response.TripChargesResponse
import com.delhivery.axle.api.response.TripPaymentsResponse
import com.delhivery.axle.api.response.TripPaymentsResponse.ChargeType
import com.delhivery.axle.data.AwaitingPODUpload
import com.delhivery.axle.data.BalancePaid
import com.delhivery.axle.data.PODUploaded
import com.delhivery.axle.data.TripHistoryItem
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.TripStatus
import com.delhivery.axle.databinding.ActivityTripDetailsBinding
import com.delhivery.axle.databinding.ViewPaymentBreakupItemBinding
import com.delhivery.axle.databinding.ViewPaymentSummaryItemBinding
import com.delhivery.axle.databinding.ViewTripHistoryItemBinding
import com.delhivery.axle.databinding.ViewTripHistoryPodUploadedBinding
import com.delhivery.axle.databinding.ViewTripPaymentSummaryBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.AWSUtils
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
import java.io.File
import javax.inject.Inject

/**
 * Trip detail screen
 */
class TripDetailsActivity : BaseActivity<ActivityTripDetailsBinding, TripDetailsViewModel>(),
    AWSProgressInterface {

  init {
    hasInlineProgress = true
  }

  override fun getViewModelClass() = TripDetailsViewModel::class.java

  override fun layoutId() = R.layout.activity_trip_details

  override fun requireConnection() = true

  @Inject lateinit var awsUtils: AWSUtils

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
    viewModel.transactionId = "315900507667065"
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /* observe trip details live data */
    viewModel.progressLiveData.observe(this, ProgressObserver())
    viewModel.tripLiveData.observe(this, TransactionObserver())
    viewModel.warehouseLiveData.observe(this, Observer {
      binding.labelWarehouse.text = it
    })
    viewModel.delegationLiveData.observe(this, Observer {
      if (it != null) {
        awsUtils.startDownload(it.first, it.second, it.third, this)
      } else {
        uiUtils.showSnackbar("Please try again")
      }
    })

    viewModel.historyLiveData.observe(this, Observer {
      if (!viewModel.chargesSummary.isNullOrEmpty() &&
          viewModel.tripDetail.tripStatus == TripStatus.TripCompleted.statusKey
      ) {
        populatePaymentSummary(viewModel.chargesSummary.toMutableList())
      } else {
        populateHistory(viewModel.tripHistory.toSortedMap().values.toMutableList())
      }
    })

    binding.viewHistory.setOnClickListener {
      populateHistory(viewModel.tripHistory.toSortedMap().values.toMutableList())
    }

    binding.viewSummary.setOnClickListener {
      populatePaymentSummary(viewModel.chargesSummary.toMutableList())
    }

    binding.containerError.btnAction.setOnClickListener {
      refreshData()
    }

    binding.refreshLayout.setOnRefreshListener {
      refreshData()
    }

    refreshData()
  }

  private fun refreshData() {
    binding.refreshing = true
    binding.error = false
    viewModel.tripHistory.clear()
    viewModel.chargesSummary.clear()
    viewModel.paymentsSummary.clear()
    viewModel.fetchTripDetails()
    binding.executePendingBindings()
  }

  /**
   * Transaction details and UI updation Observer
   */
  inner class TransactionObserver : Observer<Pair<HomeBidsRequestItemData, HomeTripsItemData>> {
    override fun onChanged(t: Pair<HomeBidsRequestItemData, HomeTripsItemData>?) {
      binding.refreshing = false
      if (t != null) {
        binding.error = false
        title = t.first.tripDisplayName(t.second.tripStatus())
        binding.transaction = t.first
        binding.tripDetails = t.second
        binding.textPromiseDate.setTextColor(
            ContextCompat.getColor(baseContext, t.second.requiredPromiseDateColor())
        )
        viewModel.bidDetail = t.second.bidDetails
        viewModel.fetchWarehouseDetails()
        viewModel.fetchPaymentSummary()
        viewModel.fetchChargeSummary()
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

  private fun populateHistory(history: MutableList<TripHistoryItem>) {
    // Capture event
    analyticsUtil.trackEvent(
        EVENT_TRIP_STATUS_HISTORY,
        mutableListOf(PROPERTY_TRANSACTION_ID),
        mutableListOf(viewModel.transactionId)
    )
    binding.progressHistory.root.visibility = View.GONE
    binding.viewHistory.isSelected = true
    binding.textStatusHistory.setTextColor(ContextCompat.getColor(this, R.color.black))
    binding.viewSummary.isSelected = false
    binding.textPaymentSummary.setTextColor(ContextCompat.getColor(this, R.color.transparent_grey))

    binding.containerHistory.removeAllViews()
    var index = 0
    history.forEach { item ->
      when (item.id) {
        BalancePaid -> {
          ViewTripHistoryItemBinding.inflate(
              layoutInflater, binding.containerHistory, false
          )
              .apply {
                focusView = false
                if (index == 0) {
                  val background = item.getBackground()
                  container.setBackgroundResource(background)
                  focusView = background != R.color.white
                }
                setHistory(item)
                binding.containerHistory.addView(root)
              }
        }
        PODUploaded -> {
          ViewTripHistoryPodUploadedBinding.inflate(layoutInflater, binding.containerHistory, false)
              .apply {
                focusView = false
                if (index == 0) {
                  val background = item.getBackground()
                  container.setBackgroundResource(background)
                  focusView = background != R.color.white
                }
                setHistory(item)
                podAction = viewModel.tripDetail.podAction()
                textAction.setOnClickListener {
                  if (viewModel.tripDetail.podUrl.isNotNullOrEmpty())
                    requestStoragePermission()
                }
                textReupload.setOnClickListener {
                  it.post {
                    startActivityForResult(
                        uploadImageIntent(it.context, viewModel.transactionId), REQCODE_UPLOAD_POD
                    )
                  }
                }
                binding.containerHistory.addView(root)
              }
        }
        AwaitingPODUpload -> {
          ViewTripHistoryPodUploadedBinding.inflate(layoutInflater, binding.containerHistory, false)
              .apply {
                focusView = false
                if (index == 0) {
                  val background = item.getBackground()
                  container.setBackgroundResource(background)
                  focusView = background != R.color.white
                }
                setHistory(item)
                textAction.text = "Upload ePod"
                textAction.setOnClickListener {
                  it.post {
                    startActivityForResult(
                        uploadImageIntent(it.context, viewModel.transactionId), REQCODE_UPLOAD_POD
                    )
                  }
                }
                binding.containerHistory.addView(root)
              }
        }
        else -> {
          ViewTripHistoryItemBinding.inflate(layoutInflater, binding.containerHistory, false)
              .apply {
                focusView = false
                if (index == 0) {
                  val background = item.getBackground()
                  container.setBackgroundResource(background)
                  focusView = background != R.color.white
                }
                setHistory(item)
                binding.containerHistory.addView(root)
              }
        }
      }
      index++
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

  private fun populatePaymentSummary(tripChargesSummary: MutableList<TripChargesResponse>) {
    analyticsUtil.trackEvent(
        EVENT_PAYMENT_SUMMARY,
        mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
        mutableListOf(VALUE_LOAD, viewModel.transactionId)
    )
    binding.progressHistory.root.visibility = View.GONE
    binding.viewSummary.isSelected = true
    binding.textPaymentSummary.setTextColor(ContextCompat.getColor(this, R.color.black))
    binding.viewHistory.isSelected = false
    binding.textStatusHistory.setTextColor(ContextCompat.getColor(this, R.color.transparent_grey))

    binding.containerHistory.removeAllViews()
    val paymentSummaryBinding = ViewTripPaymentSummaryBinding.inflate(
        layoutInflater, binding.containerHistory, false
    )

    var total = 0.0
    tripChargesSummary.add(
        0, TripChargesResponse(
        ChargeType.Freight.charge_key, binding.tripDetails?.bidDetails?.bidPrice ?: 0.0,
        0.0, null, ""
    )
    )

    tripChargesSummary.forEach { charge ->
      if (charge.payVendor != null && charge.payVendor != 0.0) {
        ViewPaymentSummaryItemBinding.inflate(
            layoutInflater, paymentSummaryBinding.containerPayment, false
        )
            .apply {
              if (viewModel.tripDetail.chargesUpdated == true ||
                  (charge.head == "loading_charge" && (viewModel.tripDetail.tripStatus != TripStatus.TruckArrived.statusKey ||
                      viewModel.tripDetail.tripStatus != TripStatus.TruckLoaded.statusKey)) ||
                  charge.head == "freight"
              ) {
                data = charge
                seprator.visibility = View.GONE
                if (charge.payVendor < 0) {
                  total -= charge.payVendor
                  textChargeType.setTextColor(
                      ContextCompat.getColor(
                          this@TripDetailsActivity,
                          R.color.status_lost
                      )
                  )
                  textChargeValue.setTextColor(
                      ContextCompat.getColor(
                          this@TripDetailsActivity,
                          R.color.status_lost
                      )
                  )
                } else {
                  total += charge.payVendor
                }
                paymentSummaryBinding.containerPayment.addView(root)
              }
            }
      }

      if (charge.deductVendor != null && charge.deductVendor != 0.0) {
        ViewPaymentSummaryItemBinding.inflate(
            layoutInflater, paymentSummaryBinding.containerPayment, false
        )
            .apply {
              if (viewModel.tripDetail.chargesUpdated == true || (charge.head == "loading_charge" &&
                      (viewModel.tripDetail.tripStatus != TripStatus.TruckArrived.statusKey ||
                          viewModel.tripDetail.tripStatus != TripStatus.TruckLoaded.statusKey)) ||
                  charge.head == "freight"
              ) {
                data = charge
                seprator.visibility = View.GONE
                textChargeType.setTextColor(
                    ContextCompat.getColor(
                        this@TripDetailsActivity,
                        R.color.status_lost
                    )
                )
                textChargeValue.setTextColor(
                    ContextCompat.getColor(
                        this@TripDetailsActivity,
                        R.color.status_lost
                    )
                )
                total -= charge.deductVendor
                paymentSummaryBinding.containerPayment.addView(root)
              }
            }
      }
    }

    ViewPaymentSummaryItemBinding.inflate(
        layoutInflater, paymentSummaryBinding.containerPayment, false
    )
        .apply {
          seprator.visibility = View.VISIBLE
          data = TripChargesResponse(
              ChargeType.SubTotal.charge_key, total,
              0.0, null, ""
          )
          paymentSummaryBinding.containerPayment.addView(root)
        }

    var tds = 0.0
    val advance = viewModel.bidDetail?.advancePayout ?: 0.0

    val payments = mutableListOf<TripPaymentsResponse>()
    if (viewModel.paymentsSummary.isEmpty()) {
      val advancePayment = TripPaymentsResponse("advance_pending", "", advance, "")
      tds += advancePayment.getTDS(viewModel.userPrefs.tdsRate, viewModel.userPrefs.updatedTdsRate)
      payments.add(0, advancePayment)

      val pending = TripPaymentsResponse(
          "balance_pending", "",
          total.minus(advance), "",
          when {
            viewModel.tripDetail.damagePending == true -> "Damage Issue"
            viewModel.tripDetail.detentionPending == true -> "Detention Issue"
            else -> ""
          }
      )
      tds += pending.getTDS(viewModel.userPrefs.tdsRate, viewModel.userPrefs.updatedTdsRate)
      payments.add(1, pending)
    } else {
      val paymentMap = mutableMapOf<String, TripPaymentsResponse>()
      val advancePayment = viewModel.paymentsSummary.find { it.head == "cash_advance" }
      val loadingPayment = viewModel.paymentsSummary.find { it.head == "loading_charge" }
      var totalAdvance = 0.0
      advancePayment?.let { it ->
        totalAdvance += it.amount
        loadingPayment?.let { it1 ->
          totalAdvance += it1.amount
          if (it1.amount > 0) {
            tds += it1.getTDS(viewModel.userPrefs.tdsRate, viewModel.userPrefs.updatedTdsRate)
          }
        }
        if (it.amount > 0) {
          tds += it.getTDS(viewModel.userPrefs.tdsRate, viewModel.userPrefs.updatedTdsRate)
        }
        paymentMap["advance_paid"] = TripPaymentsResponse(
            "advance_paid", it.bankTransactionId ?: "",
            totalAdvance, it.transferTime ?: "", it.remark ?: ""
        )
      }

      var interPayments = 0.0
      val intermittentPayments = viewModel.paymentsSummary.filter { it.head == "intermittent" }
      if (!intermittentPayments.isNullOrEmpty()) {
        intermittentPayments.forEachIndexed { index, intermittentPayout ->
          val head = intermittentPayout.head + " " + (index + 1)
          if (intermittentPayout.amount > 0) {
            tds += intermittentPayout.getTDS(
                viewModel.userPrefs.tdsRate, viewModel.userPrefs.updatedTdsRate
            )
          }
          interPayments += intermittentPayout.amount
          paymentMap[head] = intermittentPayout
          paymentMap[head] = TripPaymentsResponse(
              head, intermittentPayout.bankTransactionId ?: "",
              intermittentPayout.amount, intermittentPayout.transferTime ?: "",
              intermittentPayout.remark ?: ""
          )
        }
      }

      val partialBalancePayment =
        viewModel.paymentsSummary.find { it.head == "partial_balance_payment" }
      partialBalancePayment?.let {
        interPayments += it.amount
        paymentMap["partial_balance_payment"] = TripPaymentsResponse(
            "partial_balance_payment", it.bankTransactionId ?: "",
            it.amount, it.transferTime ?: "", it.remark ?: ""
        )
        if (it.amount > 0)
          tds += it.getTDS(viewModel.userPrefs.tdsRate, viewModel.userPrefs.updatedTdsRate)
      }

      val balancePayment = viewModel.paymentsSummary.find { it.head == "balance_payment" }
      if (balancePayment != null) {
        paymentMap["balance_paid"] = TripPaymentsResponse(
            "balance_paid", balancePayment.bankTransactionId,
            balancePayment.amount, balancePayment.transferTime ?: "", balancePayment.remark ?: ""
        )
        if (balancePayment.amount > 0) tds += balancePayment.getTDS(
            viewModel.userPrefs.tdsRate, viewModel.userPrefs.updatedTdsRate
        )
      } else {
        val pending = TripPaymentsResponse(
            "balance_pending", "",
            total.minus(totalAdvance + interPayments), "", when {
          viewModel.tripDetail.damagePending == true -> "Damage Issue"
          viewModel.tripDetail.detentionPending == true -> "Detention Issue"
          else -> ""
        }
        )
        paymentMap["balance_pending"] = pending

        if (pending.amount > 0)
          tds += pending.getTDS(viewModel.userPrefs.tdsRate, viewModel.userPrefs.updatedTdsRate)
      }

      ViewPaymentSummaryItemBinding.inflate(
          layoutInflater, paymentSummaryBinding.containerPayment, false
      )
          .apply {
            seprator.visibility = View.GONE
            data = TripChargesResponse(ChargeType.TDS.charge_key, null, tds, "", "")
            textChargeValue.setTextColor(
                ContextCompat.getColor(this@TripDetailsActivity, R.color.status_lost)
            )

            paymentSummaryBinding.containerPayment.addView(root)
          }

      paymentSummaryBinding.total = "₹ ${StringUtils.formatAmount((total - tds))}"

      payments.apply { addAll(paymentMap.values) }
    }

    payments.forEach { _payment ->
      ViewPaymentBreakupItemBinding.inflate(
          layoutInflater, paymentSummaryBinding.containerPaymentBreakup, false
      )
          .apply {
            _payment.amount = _payment.amount - _payment.getTDS(
                viewModel.userPrefs.tdsRate, viewModel.userPrefs.updatedTdsRate
            )
            data = _payment
            paymentSummaryBinding.containerPaymentBreakup.addView(root)
          }
    }

    paymentSummaryBinding.containerTotal.visibility = View.VISIBLE
    binding.containerHistory.addView(paymentSummaryBinding.root)
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
}

/* intent keys */
private const val TransactionIdIntentKey = "transaction_id"

/**
 * Trip details intent
 */
fun tripDetailsIntent(
  transactionId: String,
  context: Context
) = Intent(context, TripDetailsActivity::class.java).apply {
  putExtra(TransactionIdIntentKey, transactionId)
}

