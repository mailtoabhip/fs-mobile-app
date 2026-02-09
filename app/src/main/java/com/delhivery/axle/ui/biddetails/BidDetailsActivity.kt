package com.delhivery.axle.ui.biddetails

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Layout
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.biddetail.BulkBidSummaryItemData
import com.delhivery.axle.data.biddetail.EXPAND_CARD
import com.delhivery.axle.data.biddetail.OPEN_CONFIRMED_BID
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.TransactionBidStatus
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.trucks.TruckFrequentItem
import com.delhivery.axle.databinding.*
import com.delhivery.axle.fcm.ARGS_DEEPLINK_ID
import com.delhivery.axle.fcm.ARGS_DEEPLINK_TYPE
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.bids.BidType
import com.delhivery.axle.ui.bids.userBidsIntent
import com.delhivery.axle.ui.dialogs.BidConfirmReviseDialog
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.home.activity.home.homeActivityIntent
import com.delhivery.axle.ui.home.fragments.HomeFragmentType
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.StringUtils.capitalize
import com.delhivery.axle.utils.extensions.isNotEmpty
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.APPROVED
import com.delhivery.axle.utils.prefs.DISABLED
import com.delhivery.axle.utils.prefs.UNAPPROVED
import com.delhivery.axle.utils.prefs.UserPrefs
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.ContactsContract
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import com.delhivery.axle.api.request.UpdateVehicleDetailsRequest
import com.delhivery.axle.data.home.placements.HomePlacementsItemData
import com.delhivery.axle.databinding.DialogPlacementDetailsEditBinding
import com.delhivery.axle.ui.dialogs.AddTruckBottomSheetDialogFragment
import com.delhivery.axle.ui.home.fragments.placements.LoadTypes
import com.delhivery.axle.ui.home.fragments.placements.PlacementTypes
import com.delhivery.axle.ui.placementdetails.REFRESH_ON_BACK_PLACEMENT
import com.delhivery.axle.utils.AutoCompleteUtils
import com.delhivery.axle.utils.DetailsSubmittedSuccessInterface
import com.delhivery.axle.utils.extensions.focusClick
import com.delhivery.axle.utils.extensions.getSerializableExtra
import com.google.gson.Gson
import java.util.regex.Pattern
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.lang.StringBuilder
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.abs

/**
 * Bid detail screen
 */
class BidDetailsActivity : BaseActivity<ActivityLoadBidDetailsBinding, BidDetailsViewModel>(), BulkBidsRVAdapterInterface,BidSuccessInterface {

  init {
    hasInlineProgress = true
  }

  @Inject lateinit var userPrefs: UserPrefs
  @Inject lateinit var autoCompleteUtils: AutoCompleteUtils

  private var isValidVehicleNumber = false
  private var isValidDriverName = false
  private var isValidDriverNumber = false
  private var hasUserInteractedWithDriverName = false
  private var lastValidationTime = 0L
  private var isValidationDisabled = false
  private var lastEnableSubmitTime = 0L
  private var homePlacementsItemData: HomePlacementsItemData? = null

  companion object {
    private const val REQCODE_PICK_CONTACT = 2001
  }

  override fun getViewModelClass() = BidDetailsViewModel::class.java

  override fun layoutId() = R.layout.activity_load_bid_details

  override fun requireConnection() = true

  var bidEndingTime: String = ""
  var source: String = VALUE_APP_FLOW
  var subSource: String = "NA"
  var reviseInitiated: Boolean = false
  var oldAmountbids = ""
  var isFirstBid = false
  var buttonVisible = false
  var bottomLayVisible = false
  private val adapter: BulkBidsRVAdapter by lazy { BulkBidsRVAdapter(this) }
  var uploadArray: ArrayList<Pair<String, String?>> = ArrayList()
  var stopArray: ArrayList<String> = ArrayList()
  var ellp = true
  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true
  private var amount = 0
  private var pmtRate = 0
  private var forPlacement =  false
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("BidDetailsActivity_SetupTime")
    activitySetupTrace?.start()
    /* validate intent */
    try {
      require(
        !(intent == null || !intent.hasExtra(TransactionIdIntentKey))
      ) { "Required data $TransactionIdIntentKey not found" }
    } catch (e: Exception) {
      finish()
    }
    onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        userPrefs.setPreviousScreen(this.javaClass.name)
        finish()
      }
    })
    viewModel.restrictEventTrigger = true
    /* set transaction id */
    viewModel.transactionId = intent.getStringExtra(TransactionIdIntentKey) ?: ""
    viewModel.dmtStatus = intent.getStringExtra(RequestTypeIntentKey) ?: ""
    viewModel.fromPage = intent.getBooleanExtra(FromPage, false)
    viewModel.active = intent.getBooleanExtra(ActiveBid, false)
    source = intent.getStringExtra(PROPERTY_SOURCE) ?: VALUE_APP_FLOW
    subSource = intent.getStringExtra(PROPERTY_SUB_SOURCE) ?: "NA"
    forPlacement =   intent.getBooleanExtra(ForPlacementKey, false)
    if (intent?.extras?.getSerializableExtra(PlacementData, HomePlacementsItemData::class.java) != null)
      homePlacementsItemData = intent.extras?.getSerializableExtra(PlacementData, HomePlacementsItemData::class.java)
    val addressDetailAdapter: AddressDetailAdapter = AddressDetailAdapter(uploadArray)
    binding.cvRouteSection.addresslist.apply {
      layoutManager = LinearLayoutManager(applicationContext)
      adapter = addressDetailAdapter
    }
  }

  override fun onResume() {
    super.onResume()
    if (activitySetupTrace != null && isFirstResume) {
      activitySetupTrace?.stop()
      isFirstResume = false
    }
  }
  private fun setTimer(bidEndingTime: String) {

    if (!bidEndingTime.equals("null")) {
      val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
      format.setTimeZone(TimeZone.getTimeZone("IST"));
      val date1: Date = format.parse(format.format(Date()))
      val date2: Date = format.parse(bidEndingTime)
      if (date2.compareTo(date1) > 0) {
//        binding.timerLayout.visibility = View.VISIBLE
        val mills: Long = date2.getTime() - date1.getTime()
        object : CountDownTimer(mills, 1000) {
          override fun onTick(millisUntilFinished: Long) {
            try {
              val hours = (millisUntilFinished / (1000 * 60 * 60)).toInt()
              val mins = (millisUntilFinished / (1000 * 60)).toInt() % 60
              val secs = ((millisUntilFinished / 1000).toInt() % 60).toLong()
              val format = "%1$02d"
              val hrs = String.format(format, hours)
              val ms = String.format(format, mins)
              val sec = String.format(format, secs)
              val diff = "$hrs:$ms:$sec" + "s"
//              binding.timerTime.setText(diff)
            } catch (e: Exception) {
              e.printStackTrace()
            }
          }

          override fun onFinish() {
//            binding.timerLayout.visibility = View.GONE
          }
        }.start()
      } else {
//        binding.timerLayout.visibility = View.GONE
      }
    } else {
//      binding.timerLayout.visibility = View.GONE
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
    title = ""//""Order ID - " + viewModel.transactionId
    if(homePlacementsItemData?.status== PlacementTypes.Delayed.name){
      binding.toolbarEndText.visibility = View.VISIBLE
      binding.toolbarEndText.text =
        "Delayed"
      binding.toolbarEndText.background =
        ContextCompat.getDrawable(this, R.drawable.bg_all_rounded_delayed)
    }else{
      binding.toolbarEndText.visibility = View.GONE

    }
    /* setup live data observers */
    viewModel.progressLiveData.observe(this, ProgressObserver())
    viewModel.transactionLiveData.observe(this, TransactionObserver())
    viewModel.transactionBidLiveData.observe(this, TransactionBidObserver())
    viewModel.errorBiddingLiveData.observe(this) {
      uiUtils.hideProgress()
    }
    viewModel.bidPriceLiveData.observe(this, Observer {
      if (it != null) {
        binding.transaction?.transactionBid = it
        val visibility =
          if (binding.transaction?.bidAmount()
              .isNullOrEmpty()
          ) View.GONE else View.VISIBLE
      /*  binding.priceLay.visibility = visibility
        binding.textTargetPrice.visibility = visibility
        binding.textTargetPriceLabel.visibility = visibility
        if (visibility == View.VISIBLE) {
          binding.priceLay.visibility = View.VISIBLE
          if (binding.transaction?.isPMTIndent() == true) {
            binding.textTargetPrice.text = binding.transaction?.bidAmount() + "/MT"
          } else {
            binding.textTargetPrice.text = binding.transaction?.bidAmount()

          }
          binding.textTargetPriceLabel.text = binding.transaction?.amountLabel()
        } else {
          binding.priceLay.visibility = View.GONE
        }*/
      }
    })
    viewModel.successBidLiveData.observe(this) {
      if (it.first) {
      val dialog =  dialogUtils.showSuccessBidDialog(this,
          resources.getString(R.string.bid_placed_sucessfully),
          resources.getString(R.string.check_your_bid_status)
        )
        navigateToBid(dialog)
      }else if(it.second){
      val dialog =  dialogUtils.showSuccessBidDialog(this,
          resources.getString(R.string.bid_revised_sucessfully),
          resources.getString(R.string.check_your_bid_revise_status)
        )
        navigateToBid(dialog)

      }
    }
    viewModel.reviseBidLiveData.observe(this, Observer {
          if (it.first) {
//           bidDialog(it.second)
          }
        })
    viewModel.userBidsData.observe(this, Observer {
      if (it != null) {
        adapter.operation(it)
      }
    })
    viewModel.statusConfirmationPending.observe(this, Observer {
      if (it) {
//        binding.status.visibility = View.VISIBLE
//        binding.status.text = resources.getString(R.string.label_pending)
//        binding.status.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.pending_bg))
//        binding.status.setTextColor(ContextCompat.getColor(applicationContext, R.color.pending_status))
      }
    })
    viewModel.truckGetLiveData.observe(this, Observer {
      uiUtils.hideProgress()
      if (it != null) {
        val pageTitle =
          if (it.second.bulkTransactionBids != null && it.second.bulkTransactionBids.isNotEmpty()) "EDIT BIDS" else "PLACE BIDS"
        if (it.second.bulkTransactionBids != null && it.second.bulkTransactionBids.isNotEmpty()) {
          for (transactionBid in it.second.bulkTransactionBids) {
            if (oldAmountbids.isNullOrEmpty()) {
              oldAmountbids = transactionBid.bidAmount.toString()
            } else {
              oldAmountbids = oldAmountbids + "," + transactionBid.bidAmount.toString()
            }
          }
          analyticsUtil.moEngageTrackEvent(
            EVENT_BID_REVISE_INITIATED,
            mutableListOf(
              PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE,
              PROPERTY_SOURCE,
              PROPERTY_SUB_SOURCE
            ),
            mutableListOf(
              it.second.uuid.toString(), viewModel.bidCount.toString(),
              viewModel.lowestBid.toString(), source, subSource
            )
          )
          reviseInitiated = true
        } else {
          analyticsUtil.moEngageTrackEvent(
            EVENT_ORDER_DETAILS_BID_INITIATE,
            mutableListOf(
              PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_SOURCE,
              PROPERTY_SUB_SOURCE
            ),
            mutableListOf(
              it.second.uuid.toString(),
              viewModel.bidCount.toString(),
              source,
              subSource
            )
          )
        }
        if (it.second.truckUUID != null) {
          BulkBidDetailsCreateEditDialog(
            this@BidDetailsActivity, it.second, it.second.bulkTransactionBids, it.first,
            viewModel, it.second.unAllocatedVolume!!, analyticsUtil = analyticsUtil,
            userPrefs = userPrefs, fromPage = "load_detail", pageTitle = pageTitle
          ).show()
        } else {
          Toast.makeText(this, "No Vehicle Types Found", Toast.LENGTH_SHORT)
            .show()

        }
      }
    })

    viewModel.editBulkLiveData.observe(this, Observer {
      if (it.first == 10) {
        Toast.makeText(this, "Bids Created Successfully", Toast.LENGTH_SHORT)
          .show()
      }
      if (it.first == 20) {
        viewModel.refreshCalled = true
        refreshData()
        Toast.makeText(this, "Bids Updated Successfully", Toast.LENGTH_SHORT)
          .show()
      }
      if (it.first == 30) {
        viewModel.refreshCalled = true
        refreshData()
        Toast.makeText(this, "Bids Deleted Successfully", Toast.LENGTH_SHORT)
          .show()
      }
      if (viewModel.editFlg[0] && viewModel.editFlg[1] && viewModel.editFlg[2]) {
        viewModel.fetchTransactionBids()
        viewModel.editFlg = mutableListOf(false, false, false)
      }
    })

    binding.containerError.btnAction.setOnClickListener {
      refreshData()
    }

    binding.refreshLayout.setOnRefreshListener {
      viewModel.refreshCalled = true
      binding.mainCl.visibility = View.GONE
      refreshData()
    }

    viewModel.indentLiveData.observe(this, Observer {
      if (!it.isEmpty()) {
        runOnUiThread {

          uploadArray.clear()

          if (binding.transaction?.pickupLocationAddress.isNotNullOrEmpty()) {
            uploadArray.add(Pair("Pickup Address", binding.transaction?.pickupLocationAddress))
          } else if (binding.transaction?.loadingLocationPincode.isNotNullOrEmpty()) {
            uploadArray.add(
              Pair(
                "Pickup Address",
                binding.transaction?.loadingLocationPincode.toString() + "-" + binding.transaction?.pickupLocationCity
              )
            )
          }

          val sortedList = it.sortedWith(compareBy({ it.first.first }))

          val j = 0
          for (i in sortedList) {
            j + 1
            uploadArray.add(Pair("Intermediary Stop (Additional Pickup)", i.second + " " + i.third))
          }

          if (binding.transaction?.dropLocationAddress.isNotNullOrEmpty()) {
            uploadArray.add(
              Pair(
                "Drop Address",
                binding.transaction?.dropLocationAddress.toString()
              )
            )
          } else if (binding.transaction?.unloadingLocationPincode.isNotNullOrEmpty()) {
            uploadArray.add(
              Pair(
                "Drop Address",
                binding.transaction?.unloadingLocationPincode.toString() + "-" + binding.transaction?.dropLocationCity.toString()
              )
            )
          }

          if (!uploadArray.isEmpty()) {
            binding.cvRouteSection.addressLay.visibility = View.VISIBLE
            val addressDetailAdapter = AddressDetailAdapter(uploadArray)
            binding.cvRouteSection.addresslist.apply {
              layoutManager = LinearLayoutManager(applicationContext)
              adapter = addressDetailAdapter
            }
          } else {
            binding.cvRouteSection.addressLay.visibility = View.GONE
          }

        }
      }
    })

    binding.cardInput.etBidAmount.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) = Unit
      override fun beforeTextChanged(
        s: CharSequence?,
        start: Int,
        count: Int,
        after: Int
      ) = Unit

      override fun onTextChanged(
        s: CharSequence?,
        start: Int,
        before: Int,
        count: Int
      ) {
        if (s != null) {
       //   binding.tilAmount.error = null
       //   binding.tilAmount.isErrorEnabled = false
          try {
            val input = s.trim()
              .toString()
              .toInt()
            val data = binding.transaction as HomeBidsRequestItemData
              /**
               * Check if its a PMT load or FTL load
               */
            if (data.isPMTIndent()) {
              pmtRate = input

                /**
                 * Validate PMT rate against maximum allowed rate
                 * Disable bid placement if rate exceeds user's maximum PMT rate limit
                 */
              if (pmtRate > userPrefs.maxPMTRate) {
                enablePlaceBid(false)
                binding.cardInput.bidError.visibility = View.VISIBLE
                binding.cardInput.bidError.text = "*Rate should be less than ${userPrefs.maxPMTRate}/MT"
               // throw Exception("*Rate should be less than ${userPrefs.maxPMTRate}/MT")
              }
              //amount = (input * data.requestedCapacityMg).toInt()
            //  binding.labelBid.text = "Your minimum payout will be ₹ $amount"

              /**
               * Validate bid difference for PMT loads with existing bids
               * Ensure new bid differs by at least ₹500 from existing bid amount
               */
              else if (data.transactionBid != null) {
                if (abs((input * data.requestedCapacityMg) - (data.transactionBid?.pmtRate
                    ?: 0.0)) < 500) {
                  enablePlaceBid(false)
                  binding.cardInput.bidError.visibility = View.VISIBLE
                  binding.cardInput.bidError.text = "*Bid difference should be more than ₹500"

                //  throw Exception("*Bid difference should be more than ₹500")
                }  else {
                    enablePlaceBid(true)
                    binding.cardInput.bidError.visibility = View.GONE
                }
                  /**
                   * PMT rate is valid (within max limit and no existing bid)
                   * Enable bid placement button
                   */
              } else {
                  enablePlaceBid(true)
                  binding.cardInput.bidError.visibility = View.GONE
              }
                /**
                 * Handle FTL (Full Truck Load) bid validation
                 * For non-PMT loads, validate bid amount and enable/disable placement accordingly
                 */
            } else {
              binding.cardInput.bidError.visibility = View.GONE
              amount = input
              if (data.transactionBid != null) {
                  enablePlaceBid(data.transactionBid!!.bidAmount.toInt() != input)
              }else{
                if(input>0){
                  enablePlaceBid(true)
                }else{
                  enablePlaceBid(false)
                }
              }

            }
          } catch (e: NumberFormatException) {
            enablePlaceBid(false)

//            binding.tilAmount.isErrorEnabled = true
//            binding.tilAmount.error = "*Invalid Value"
            amount = 0
//            binding.labelBid.text = ""
          } catch (e: Exception) {
            enablePlaceBid(false)
//            binding.tilAmount.isErrorEnabled = true
//            binding.tilAmount.error = e.message
            amount = 0
          }
        }else{
          enablePlaceBid(false)
        }
      }
    })

    binding.cardInput.placeBidButton.setOnClickListener {
      Log.d("Touch", "Button touched:")
      try {
      /*  require(
          !(viewModel.transaction.isPMTIndent() && pmtRate > userPrefs.maxPMTRate)
        ) { "*Rate should be less than ${userPrefs.maxPMTRate}/MT" }
        if (amount > 0) {
          if (viewModel.transaction.isPMTIndent()) {
            val costPerKm = pmtRate / viewModel.transaction.distance
            if (costPerKm > userPrefs.maxCostPerKM) {
              //isChecked = true
              throw IllegalArgumentException(
                "*Are you sure you want to bid ₹ ${
                  StringUtils.formatDecimalAmount(
                    costPerKm
                  )
                } /MT/KM"
              )
            }
          } else require(
            !(viewModel.transaction.transactionBid?.bidAmount != null && abs(viewModel.transaction.transactionBid!!.bidAmount - amount) < 500)
          ) { "*Bid difference should be more than ₹500" }*/
        when (viewModel.userPrefs.canBid()) {
          APPROVED -> {
            analyticsUtil.moEngageTrackEvent(
              EVENT_ADD_TRUCK_INITIATE,
              mutableListOf(PROPERTY_SOURCE),
              mutableListOf(VALUE_BANNER)
            )
            if (viewModel.transaction.transactionBid == null) {
              uiUtils.showProgress()
              viewModel.createBid(
                viewModel.transaction.isPMTIndent(), viewModel.transaction.key(), amount, pmtRate,
                viewModel.transaction.biddingType
                  ?: "FTL",null,null,null
              )
            } else {
              uiUtils.showProgress()
              viewModel.editBid(
                viewModel.transaction.isPMTIndent(),  viewModel.transaction.key(),  viewModel.transaction!!.transactionBid!!.key(),
                amount, pmtRate, viewModel.transaction.biddingType ?: "FTL",null,null,null
              )
            }
          }
          UNAPPROVED -> {
            dialogUtils.showBasicConfirmDialog(
              string.title_dialog_supplier_not_approved,
              string.msg_dialog_supplier_not_approved,
              getString(string.label_call_us), getString(string.label_mail_us),
              { callHelpline() }, { sendMail() }
            )
          }
          DISABLED -> {
            dialogUtils.showBasicConfirmDialog(
              string.title_dialog_supplier_disabled,
              string.msg_dialog_supplier_disabled,
              getString(string.label_call_us), getString(string.label_mail_us),
              { callHelpline() }, { sendMail() }
            )
          }
        }

//        } else {
//          throw IllegalArgumentException("*Invalid amount")
//        }
      } catch (e: IllegalArgumentException) {
     /*   binding.tilAmount.isErrorEnabled = true
        binding.tilAmount.error = e.message
        val shake = AnimationUtils.loadAnimation(context, R.anim.shake)
        binding.tilAmount.startAnimation(shake)*/
      }
    }

    refreshData()
  }

  fun enablePlaceBid(enable:Boolean){
    binding.cardInput.placeBidButton.isEnabled = enable
  }
  private fun refreshData() {
     binding.error = false
     viewModel.fetchTransactionDetails()

    binding.executePendingBindings()
  /*  if (binding.buttonConfirm.visibility == View.VISIBLE) {
      binding.buttonConfirm.visibility = View.GONE
      buttonVisible = true
    }
    if (binding.bottomLay.visibility == View.VISIBLE) {
      binding.bottomLay.visibility = View.GONE
      bottomLayVisible = false
    }*/
    uploadArray.clear()
  }

  fun navigateToBid(dialog: Dialog){
    var dialogCancelled = false
    dialog.setOnCancelListener {
      dialogCancelled = true
    }

    dialog.setOnDismissListener {
      dialogCancelled = true
    }
    Handler(Looper.getMainLooper()).postDelayed({
      if (dialog.isShowing) {
        dialog.dismiss()
      }

      if (!dialogCancelled && !isFinishing && !isDestroyed) {
        dialog.dismiss()
        startActivity(homeActivityIntent("load", this@BidDetailsActivity))
        finish()
      }
    }, 5000)
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
            binding.refreshing = true
          }
          false -> {
            binding.refreshLayout.isRefreshing = false
           /* if (buttonVisible) {
              binding.buttonConfirm.visibility = View.VISIBLE
            }
            if (bottomLayVisible) {
              binding.bottomLay.visibility = View.VISIBLE
            }*/
          }
        }
      }
      binding.executePendingBindings()
    }
  }

  /**
   * Transaction details and UI updation Observer
   */
  inner class TransactionObserver : Observer<HomeBidsRequestItemData?> {
    override fun onChanged(t: HomeBidsRequestItemData?) {
      if (t != null) {
        t.let { _transaction ->
          binding.cardInput.etBidAmount.isFocusable = true
          binding.detailsContent.request = _transaction
          binding.error = false
          binding.transaction = _transaction
          binding.cardInput.request = _transaction

          if (_transaction.isDMTIndent()) {
//            binding.unallocated =
//              getString(string.unallocated_bulk_order) + (_transaction.unAllocatedVolume)?.toInt() + " MT"
          }
        /*  if(_transaction!!.requiredAtDraw()!=null){
          binding.textDateTime.setCompoundDrawablesWithIntrinsicBounds(
              null, null, ContextCompat.getDrawable(
              this@BidDetailsActivity, _transaction!!.requiredAtDraw()!!
          ), null
          )
          }*/
        }


        if (binding.transaction?.indentOrigin.equals("LH")) {

        /*  if (binding.transaction?.additionalRemarks.isNotNullOrEmpty()) {
            binding.remarks.visibility = View.VISIBLE
            binding.layRem.visibility = View.VISIBLE
            binding.div2.visibility = View.VISIBLE
            binding.remarks.text = binding.transaction?.additionalRemarks
          } else {
            binding.remarks.visibility = View.GONE
            binding.layRem.visibility = View.GONE
            binding.div2.visibility = View.GONE
          }*/

          if (binding.transaction?.indentHaltCenters.isNullOrEmpty()) {
            if (binding.transaction?.pickupLocationAddress.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Address", binding.transaction?.pickupLocationAddress))
            } else if (binding.transaction?.loadingLocationPincode.isNotNullOrEmpty()) {
              uploadArray.add(
                Pair(
                  "Pickup Address",
                  binding.transaction?.loadingLocationPincode.toString() + "-" + binding.transaction?.pickupLocationCity
                )
              )
            }
            if (binding.transaction?.dropLocationAddress.isNotNullOrEmpty()) {
              uploadArray.add(
                Pair(
                  "Drop Address",
                  binding.transaction?.dropLocationAddress.toString()
                )
              )
            } else if (binding.transaction?.unloadingLocationPincode.isNotNullOrEmpty()) {
              uploadArray.add(
                Pair(
                  "Drop Address",
                  binding.transaction?.unloadingLocationPincode.toString() + "-" + binding.transaction?.dropLocationCity.toString()
                )
              )
            }
            if (!uploadArray.isEmpty()) {
              binding.cvRouteSection.addressLay.visibility = View.VISIBLE
              val addressDetailAdapter = AddressDetailAdapter(uploadArray)
              binding.cvRouteSection.addresslist.apply {
                layoutManager = LinearLayoutManager(applicationContext)
                adapter = addressDetailAdapter
              }
            } else {
              binding.cvRouteSection.addressLay.visibility = View.GONE
            }
          } else {

//            binding.stopNo.text =
//              binding.transaction?.indentHaltCenters!!.size.toString() + " Stops"
            for (i in binding.transaction?.indentHaltCenters!!.indices) {
              viewModel.fetchIndentCenters(
                binding.transaction?.indentHaltCenters!![i].haltCenterCode, i + 1
              )
            }
          }
        } else {

        /*  if (binding.transaction?.orderCreationRemarks.isNotNullOrEmpty()) {
            binding.remarks.visibility = View.VISIBLE
            binding.layRem.visibility = View.VISIBLE
            binding.div2.visibility = View.VISIBLE
            binding.remarks.text = binding.transaction?.orderCreationRemarks
          } else {
            binding.remarks.visibility = View.GONE
            binding.layRem.visibility = View.GONE
            binding.div2.visibility = View.GONE

          }*/

          var total = 0

          if (binding.transaction?.pickupLocationAddress.isNotNullOrEmpty()) {
            uploadArray.add(Pair("Pickup Address", binding.transaction?.pickupLocationAddress))
          } else {
            var uData = StringBuilder()
            if (binding.transaction?.loadingLocationPincode.isNotNullOrEmpty()) {
              uData.append(binding.transaction?.loadingLocationPincode + "-")
            }

            if (binding.transaction?.pickupLocationCity.isNotNullOrEmpty()) {
              uData.append(capitalize(binding.transaction?.pickupLocationCity!!))
            }

            if (uData.toString().isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Address", uData.toString()))
            }

          }

          if (!TextUtils.isEmpty(binding.transaction?.pickup1)) {
            total = total + 1
          //  binding.textViaDestination.card1.visibility = View.VISIBLE
            var citNam: String? = null
            if (binding.transaction?.isDmt == true) {
              citNam = capitalize(binding.transaction?.pickup1)
            } else {
              citNam = capitalize(binding.transaction?.pickup1City)
            }
         //   binding.textViaDestination.city1.text = citNam

            if (binding.transaction?.pickup1Address.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Intermediary Stop (Additional Pickup)", binding.transaction?.pickup1Address))
            } else {
              var uData = StringBuilder()
              if (binding.transaction?.pickup1AddressPin.isNotNullOrEmpty()) {
                uData.append(binding.transaction?.pickup1AddressPin + "-")
              }
              if (citNam.isNotNullOrEmpty()) {
                uData.append(citNam)
              }

              if (uData.toString().isNotNullOrEmpty()) {
                uploadArray.add(Pair("Intermediary Stop (Additional Pickup)", uData.toString()))
              }
            }
          }

          if (!TextUtils.isEmpty(binding.transaction?.pickup2)) {
            total = total + 1
         //   binding.textViaDestination.card2.visibility = View.VISIBLE

            var citNam: String? = null
            if (binding.transaction?.isDmt == true) {
              citNam = capitalize(binding.transaction?.pickup2)
            } else {
              citNam = capitalize(binding.transaction?.pickup2City)
            }
         //   binding.textViaDestination.city2.text = citNam

            if (binding.transaction?.pickup2Address.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Intermediary Stop (Additional Pickup)", binding.transaction?.pickup2Address))
            } else {

              var uData = StringBuilder()
              if (binding.transaction?.pickup2AddressPin.isNotNullOrEmpty()) {
                uData.append(binding.transaction?.pickup2AddressPin + "-")
              }
              if (citNam.isNotNullOrEmpty()) {
                uData.append(citNam)
              }

              if (uData.toString().isNotNullOrEmpty()) {
                uploadArray.add(Pair("Intermediary Stop (Additional Pickup)", uData.toString()))
              }

            }
          }

          if (!TextUtils.isEmpty(binding.transaction?.stop1)) {
            if (total == 0) {
         //     binding.textViaDestination.img3.visibility = View.GONE
            }
            total = total + 1
         //   binding.textViaDestination.card3.visibility = View.VISIBLE

            var citNam: String? = null
            if (binding.transaction?.isDmt == true) {
              citNam = capitalize(binding.transaction?.stop1)
            } else {
              citNam = capitalize(binding.transaction?.stop1City)
            }
        //    binding.textViaDestination.city3.text = citNam

            if (binding.transaction?.intermediaryStop1Address.isNotNullOrEmpty()) {
              uploadArray.add(
                Pair(
                  "Intermediary Stop (Partial Drop)", binding.transaction?.intermediaryStop1Address
                )
              )
            } else {
              var uData = StringBuilder()
              if (binding.transaction?.intermediaryStop1AddressPin.isNotNullOrEmpty()) {
                uData.append(binding.transaction?.intermediaryStop1AddressPin + "-")
              }
              if (citNam.isNotNullOrEmpty()) {
                uData.append(citNam)
              }

              if (uData.toString().isNotNullOrEmpty()) {
                uploadArray.add(Pair("Intermediary Stop (Partial Drop)", uData.toString()))
              }

            }

          }
          if (!TextUtils.isEmpty(binding.transaction?.stop2)) {
            total = total + 1
        //    binding.textViaDestination.card4.visibility = View.VISIBLE

            var citNam: String? = null
            if (binding.transaction?.isDmt == true) {
              citNam = capitalize(binding.transaction?.stop2)
            } else {
              citNam = capitalize(binding.transaction?.stop2City)
            }
       //     binding.textViaDestination.city4.text = citNam

            if (binding.transaction?.intermediaryStop2Address.isNotNullOrEmpty()) {
              uploadArray.add(
                Pair(
                  "Intermediary Stop (Partial Drop)", binding.transaction?.intermediaryStop2Address
                )
              )
            } else {

              var uData = StringBuilder()
              if (binding.transaction?.intermediaryStop2AddressPin.isNotNullOrEmpty()) {
                uData.append(binding.transaction?.intermediaryStop2AddressPin + "-")
              }
              if (citNam.isNotNullOrEmpty()) {
                uData.append(citNam)
              }

              if (uData.toString().isNotNullOrEmpty()) {
                uploadArray.add(Pair("Intermediary Stop (Partial Drop)", uData.toString()))
              }
            }
          }

        /*  if (total > 0) {
            binding.textViaLabel.visibility = View.VISIBLE
            binding.stopNo.text = "$total Stops"
          } else {
            binding.stopNo.text = "No Stops"
          }*/

          if (binding.transaction?.dropLocationAddress.isNotNullOrEmpty()) {
            uploadArray.add(Pair("Drop Address", binding.transaction?.dropLocationAddress))
          } else {

            var uData = StringBuilder()
            if (binding.transaction?.unloadingLocationPincode.isNotNullOrEmpty()) {
              uData.append(binding.transaction?.unloadingLocationPincode + "-")
            }

            if (binding.transaction?.dropLocationCity.isNotNullOrEmpty()) {
              uData.append(capitalize(binding.transaction?.dropLocationCity!!))
            }

            if (uData.toString().isNotNullOrEmpty()) {
              uploadArray.add(Pair("Drop Address", uData.toString()))
            }

          }

          if (!uploadArray.isEmpty()) {
            binding.cvRouteSection.addressLay.visibility = View.VISIBLE
            val addressDetailAdapter = AddressDetailAdapter(uploadArray)
            binding.cvRouteSection.addresslist.apply {
              layoutManager = LinearLayoutManager(applicationContext)
              adapter = addressDetailAdapter
            }
          } else {
            binding.cvRouteSection.addressLay.visibility = View.GONE
          }
        }


   //     bidEndingTime = binding.transaction!!.bidEndingTime.toString()

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

  fun enableKeyboard(){
    binding.cardInput.etBidAmount.requestFocus()

    // Delay to ensure the window is ready before showing the keyboard
    binding.cardInput.etBidAmount.post {
      val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      imm.showSoftInput(binding.cardInput.etBidAmount, InputMethodManager.SHOW_IMPLICIT)
    }
  }
  /**
   * Transaction bid details UI updation observer
   */
  inner class TransactionBidObserver : Observer<BidDetailsUserBidState?> {
    override fun onChanged(t: BidDetailsUserBidState?) {
      binding.refreshing = false
    //  binding.mainCl.visibility = View.VISIBLE
      uiUtils.hideProgress()
      t?.let { state ->
        when (state) {
          is BidDetailsUserBidState_PlaceBidFirst -> {
            binding.mainCl.visibility = View.VISIBLE
            binding.cardInput.root.visibility = View.VISIBLE
            binding.cardInput.editBidCl.visibility = View.VISIBLE
            binding.cardInput.confirmedBidCl.root.visibility = View.GONE
            binding.cardInput.rejectedBidCl.root.visibility = View.GONE
            enableKeyboard()
            /*  ViewBidDetailsPlaceBidFirstBinding.inflate(
                layoutInflater, binding.containerActions, false
              )
                .apply {
                 *//* if (binding.textBulkLoad.visibility == View.GONE) {
                  binding.timerLayout.visibility = View.VISIBLE
                  setTimer(bidEndingTime)
                }*//*
                isFirstBid = true
                if (viewModel.refreshCalled == false) {
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_PAGE_LOAD_ORDER_DETAILS_WITHOUT_EXISTING_BID,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_SOURCE, PROPERTY_SUB_SOURCE),
                    mutableListOf(viewModel.transactionId, source, subSource)
                  )
                }
             //   binding.status.visibility = View.GONE

               *//* binding.buttonConfirm.text = "Place Your Bid"
                binding.bottomLay.visibility = View.VISIBLE
                binding.buttonConfirm.setOnClickListener {
                  bidDialog()
                }*//*
              }*/
          }
          is BidDetailsUserBidState_PlaceBid -> {
            binding.mainCl.visibility = View.VISIBLE
            binding.cardInput.root.visibility = View.VISIBLE
            binding.cardInput.root.visibility = View.VISIBLE
            binding.cardInput.editBidCl.visibility = View.VISIBLE
            binding.cardInput.confirmedBidCl.root.visibility = View.GONE
            binding.cardInput.rejectedBidCl.root.visibility = View.GONE
            enableKeyboard()
          /*  ViewBidDetailsPlaceBidBinding.inflate(layoutInflater, binding.containerActions, false)
              .apply {
               *//* if (binding.textBulkLoad.visibility == View.GONE) {
                  binding.timerLayout.visibility = View.VISIBLE
                  setTimer(bidEndingTime)
                }*//*
                isFirstBid = true
                bidsRecieved = state.bidsCount
                state.lowestAndUserBidPair.second?.let {
                  lowestBid = when (state.lowestAndUserBidPair) {
                    null -> ""
                    else -> "Lowest Bid: ₹ ${
                      StringUtils.formatAmount(
                        state.lowestAndUserBidPair.second?.bidAmount ?: 0.0
                      )
                    }" + if (state.isPMTIndent) "/MT" else ""
                  }
                }

               *//* if (state.bidsCount != null && state.bidsCount > 0) {
                  binding.numBids.text = "$bidsRecieved Bids Received"
                  binding.numLay.visibility = View.VISIBLE
                } else {
                  binding.numLay.visibility = View.GONE
                }

                if (lowestBid.isNotNullOrEmpty()) {
                  binding.lowBid.text = lowestBid.toString()
                } else {
                  binding.lowBid.visibility = View.GONE
                }

                binding.buttonConfirm.text = "Place Your Bid"
                binding.bidLay.visibility = View.GONE
                binding.bottomLay.visibility = View.VISIBLE
                binding.buttonConfirm.setOnClickListener {
                  bidDialog()
                }*//*
                if (viewModel.refreshCalled == false) {
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                    mutableListOf(
                      PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_SOURCE,
                      PROPERTY_SUB_SOURCE
                    ),
                    mutableListOf(
                      viewModel.transactionId,
                      state.bidsCount.toString(),
                      source,
                      subSource
                    )
                  )
                }
             //   binding.status.visibility = View.GONE
              }*/
          }
          is BidDetailsUserBidState_EditBid -> {

            binding.mainCl.visibility = View.VISIBLE
            val data = binding.transaction as HomeBidsRequestItemData
            if(!data.isLoadBiddingOpen()){
              binding.cardInput.editBidCl.visibility = View.GONE
              binding.cardInput.root.visibility = View.VISIBLE
              binding.cardInput.awaitingBidCl.root.visibility = View.VISIBLE
              binding.cardInput.awaitingBidCl.title = "Awaiting Result"
              binding.cardInput.awaitingBidCl.subTitle = "We’ll notify you once the results are out"
              binding.cardInput.awaitingBidCl.actionLabel = "Explore New Bids"
              binding.cardInput.awaitingBidCl.btnAction.setOnClickListener {
                startActivity(homeActivityIntent("load", this@BidDetailsActivity))

                // fragmentAction(NavigateHomeFragmentAction(HomeFragmentType.PlacementsFragment))
              }
            }else{
              binding.cardInput.root.visibility = View.VISIBLE
              binding.cardInput.editBidCl.visibility = View.VISIBLE
              binding.cardInput.confirmedBidCl.root.visibility = View.GONE
              binding.cardInput.rejectedBidCl.root.visibility = View.GONE
              enableKeyboard()
              binding.transaction?.transactionBid = state.lowestAndUserBidPair.first
              enablePlaceBid(false)
              binding.cardInput.placeBidButton.text = "Revise Bid"
              if (data.isPMTIndent()) {
                //  binding.tilAmount.hint = context.getString(R.string.hint_enter_pmt_rate_value)
                data.transactionBid?.bidAmount?.let {
                  binding.cardInput.etBidAmount?.setText(DecimalFormat("#########").format(it))
                }
                data.transactionBid?.pmtRate?.let {
//                binding.labelBid.text =
//                  "Your minimum payout will be ₹${StringUtils.formatAmount(transactionBid.pmtRate)}"
                }
              } else {
                //  binding.tilAmount.hint = context.getString(R.string.hint_enter_bid_value)
                data.transactionBid?.bidAmount?.let {
                  binding.cardInput.etBidAmount.setText(DecimalFormat("#########").format(it))
                }
              }
            }

          //  binding.cardInput.etBidAmount.text = data?.transactionBid?.bidAmount
           /* ViewBidDetailsEditBidBinding.inflate(layoutInflater, binding.containerActions, false)
              .apply {
                binding.timerLayout.visibility = View.GONE
                val data = viewModel.transaction as HomeBidsRequestItemData
                data.numBids = state.bidsCount
                var oldAmount = data?.transactionBid?.bidAmount
                data.transactionBid = state.lowestAndUserBidPair.first
                bidsRecieved = state.bidsCount
                val userBid = state.lowestAndUserBidPair.first
                val lowestTBid = state.lowestAndUserBidPair.second
                lowestTBid?.let {
                  if (it.biddingType.compareTo(userBid?.biddingType ?: "") == 0) {
                    lowestBid = when (it) {
                      null -> ""
                      else -> "Lowest Bid: ₹ ${
                        StringUtils.formatAmount(
                          it.bidAmount
                        )
                      }" + if (state.isPMTIndent) "/MT" else ""
                    }
                    data.lowestBid = when (it) {
                      null -> 0.0
                      else -> it.bidAmount
                    }
                  }
                }
                if (viewModel.openConfirmBid) {
                  viewModel.openConfirmBid=false
                  BidConfirmReviseDialog(
                    this@BidDetailsActivity,data, viewModel, 0
                  ).show()
                }
                binding.numLay.visibility = View.GONE
                binding.bottomLay.visibility = View.VISIBLE

                if (userBid?.bidAmount != null) {
                  binding.bidLay.visibility = View.VISIBLE
                  if (state.isPMTIndent) {
                    binding.bidVal.text =
                      "₹ ${StringUtils.formatAmount(userBid?.bidAmount)}" + "/MT"
                  } else {
                    binding.bidVal.text = "₹ ${StringUtils.formatAmount(userBid?.bidAmount)}"
                  }
                  binding.greenBidLay.visibility = View.VISIBLE
                } else {
                  binding.bidLay.visibility = View.GONE
                }

                if (state.bidsCount != null && state.bidsCount > 0) {
                  binding.numBidsVal.text = "$bidsRecieved Bids Received"
                  binding.numBidsVal.visibility = View.VISIBLE
                  binding.greenBidLay.visibility = View.VISIBLE
                } else {
                  binding.numBidsVal.visibility = View.GONE
                }

                if (state.bidsCount != null && state.bidsCount > 1) {
                  if (lowestTBid?.bidAmount != null) {
                    if (userBid?.bidAmount?.equals(lowestTBid?.bidAmount) == true) {
                      binding.tvCorr.setBackground(
                        ContextCompat.getDrawable(this@BidDetailsActivity,
                          R.drawable.bg_all_round_corner_light_green_12
                        )
                      )
                      binding.imgCorr.visibility = View.VISIBLE
                      binding.tvCorr.text = "Your bid is the lowest"
                      binding.tvCorr.setTextColor(
                        ContextCompat.getColor(this@BidDetailsActivity, R.color.bid_placed_green)
                      )
                      binding.llBidStatus.background =  ContextCompat.getDrawable(this@BidDetailsActivity,
                        R.drawable.bg_all_round_corner_light_green_12
                      )
                    } else {
                      binding.tvCorr.setBackground(
                        ContextCompat.getDrawable(this@BidDetailsActivity,
                          R.drawable.bg_all_round_corner_light_pink_12
                        )
                      )
                      binding.llBidStatus.background = null
                      binding.imgCorr.visibility = View.GONE
                      binding.tvCorr.text = binding.transaction?.lowestbidText()
                      binding.tvCorr.setTextColor(ContextCompat.getColor(this@BidDetailsActivity as Context, R.color.bid_placed_red))
                    }
                  } else {
                    binding.tvCorr.setBackground(
                      ContextCompat.getDrawable(this@BidDetailsActivity,
                        R.drawable.bg_all_round_corner_light_pink_12
                      )
                    )
                    binding.llBidStatus.background = null
                    binding.imgCorr.visibility = View.GONE
                    binding.tvCorr.text = binding.transaction?.benchmarkPriceText()
                    binding.tvCorr.setTextColor(ContextCompat.getColor(this@BidDetailsActivity, R.color.bid_placed_red))
                  }
                } else if (userBid?.bidAmount != null && state.bidsCount != null && state.bidsCount == 1 && binding.transaction?.guidancePrice != null) {
                  if (userBid.bidAmount.compareTo(binding.transaction?.guidancePrice!!) > 0) {
                    binding.tvCorr.setBackground(
                      ContextCompat.getDrawable(this@BidDetailsActivity,
                        R.drawable.bg_all_round_corner_light_pink_12
                      )
                    )
                    binding.llBidStatus.background = null
                    binding.imgCorr.visibility = View.GONE
                    binding.tvCorr.text = binding.transaction?.benchmarkPriceText()
                    binding.tvCorr.setTextColor(ContextCompat.getColor(this@BidDetailsActivity, R.color.bid_placed_red))
                  } else {
                    binding.tvCorr.setBackground(
                      ContextCompat.getDrawable(this@BidDetailsActivity,
                        R.drawable.bg_all_round_corner_light_green_12
                      )
                    )
                    binding.llBidStatus.background =  ContextCompat.getDrawable(this@BidDetailsActivity,
                      R.drawable.bg_all_round_corner_light_green_12
                    )
                    binding.imgCorr.visibility = View.VISIBLE
                    binding.tvCorr.text = "Your bid is the lowest"
                    binding.tvCorr.setTextColor(
                      ContextCompat.getColor(this@BidDetailsActivity, R.color.bid_placed_green)
                    )
                  }
                }

                binding.buttonConfirm.text = "Edit Bid"
                binding.buttonConfirm.setOnClickListener {
                  bidDialog(userBid)
                }

                request = data
                if ((!viewModel.restrictEventTrigger && !viewModel.refreshCalled) || isFirstBid) {
                  isFirstBid = false
                  if (!reviseInitiated) {
                    analyticsUtil.moEngageTrackEvent(
                      EVENT_ORDER_DETAILS_BID_SUBMIT,
                      mutableListOf(
                        PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_USER_BID_VALUE,
                        PROPERTY_VEHICLE_REPORTING_DATE_TIME,
                        PROPERTY_SOURCE,
                        PROPERTY_SUB_SOURCE
                      ),
                      mutableListOf(
                        state.lowestAndUserBidPair.second?.transactionId ?: "",
                        state.bidsCount.toString(),
                        state.lowestAndUserBidPair.second?.bidAmount.toString(),
                        state.lowestAndUserBidPair.second?.expectedArrivalTimePickupRemark.toString(),
                        source,
                        subSource
                      )
                    )
                  } else {
                    analyticsUtil.moEngageTrackEvent(
                      EVENT_BID_REVISE_SUBMITTED,
                      mutableListOf(
                        PROPERTY_ORDER_ID, PROPERTY_BID_COUNT,
                        PROPERTY_ORDER_LOWEST_BID_VALUE,
                        PROPERTY_USER_BID_VALUE_OLD, PROPERTY_USER_BID_VALUE_NEW,
                        PROPERTY_SOURCE, PROPERTY_SUB_SOURCE
                      ),
                      mutableListOf(
                        state.lowestAndUserBidPair.second?.transactionId ?: "",
                        state.bidsCount.toString() ?: "", data?.lowestBid.toString() ?: " ",
                        oldAmount.toString() ?: "", data?.bidAmountValue()
                          .toString() ?: "", source, subSource
                      )
                    )
                    reviseInitiated = false
                  }
                } else {
                  if (viewModel.refreshCalled == false) {
                    analyticsUtil.moEngageTrackEvent(
                      EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                      mutableListOf(
                        PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_SOURCE,
                        PROPERTY_SUB_SOURCE
                      ),
                      mutableListOf(
                        viewModel.transactionId,
                        bidsRecieved.toString(),
                        source,
                        subSource
                      )
                    )
                  }
                  viewModel.refreshCalled = false
                }
                viewModel.restrictEventTrigger = false


                if (data.threeVisibility() == View.VISIBLE || data.fourVisibility() == View.VISIBLE) {
                  val mHandler = Handler(Looper.myLooper()!!)
                  var mRunnable: Runnable = Runnable { }
                  mRunnable = object : Runnable {
                    override fun run() {
                      btnEditBidInsider.setVisibility(View.VISIBLE)
                      //loading our custom made animations
                      val animation = AnimationUtils.loadAnimation(
                        applicationContext, R.anim.fade_in
                      )
                      //starting the animation
                      btnEditBidInsider.startAnimation(animation)
                      val animation2 = AnimationUtils.loadAnimation(
                        applicationContext, R.anim.fade_out
                      )
                      btnEditBidInsider.startAnimation(animation2)
                      mHandler.postDelayed({
                        btnEditBidInsider.setVisibility(View.GONE)
                      }, 3000)
                      mHandler.postDelayed(mRunnable, 2000)

                    }
                  }
                  mHandler.post(mRunnable)
                }

                btnEditBidInsider.setOnClickListener(View.OnClickListener {
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_NOT_LOWEST_BID_CTA,
                    mutableListOf(PROPERTY_ORDER_ID),
                    mutableListOf(
                      state.lowestAndUserBidPair.second?.transactionId ?: ""
                    )
                  )
                  bidDialog(userBid)
                })
                textEditBid.setOnClickListener(View.OnClickListener {
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_LOWEST_BID_CTA,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT),
                    mutableListOf(
                      state.lowestAndUserBidPair.second?.transactionId ?: "",
                      state.bidsCount.toString()
                    )
                  )
                  bidDialog(userBid)
                })
                textEditBid2.setOnClickListener(View.OnClickListener {
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_LOWEST_BID_CTA,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT),
                    mutableListOf(
                      state.lowestAndUserBidPair.second?.transactionId ?: "",
                      state.bidsCount.toString()
                    )
                  )
                  bidDialog(userBid)
                })
                binding.status.visibility = View.VISIBLE
                binding.status.text = resources.getString(R.string.label_active)
                binding.status.setBackgroundColor(ContextCompat.getColor(this@BidDetailsActivity, R.color.status_active))
                binding.status.setTextColor(ContextCompat.getColor(this@BidDetailsActivity, R.color.status_active))
              }*/
          }
          is BidDetailsUserBidState_LoadingBids -> {
            binding.cardInput.root.visibility = View.GONE
           /* ViewBidDetailsLoadingBidsBinding.inflate(
              layoutInflater, binding.containerActions, false
            )*/
          }
          is BidDetailsUserBidState_ConfirmedBid -> {
            if(forPlacement){
              binding.mainCl.visibility = View.VISIBLE
              binding.cardInput.editBidCl.visibility = View.GONE
              binding.cardInput.root.visibility = View.VISIBLE
              binding.cardInput.placementCl.root.visibility = View.VISIBLE
              placementInput()
            }else{
              binding.mainCl.visibility = View.VISIBLE
              binding.cardInput.editBidCl.visibility = View.GONE
              binding.cardInput.root.visibility = View.VISIBLE
              binding.cardInput.confirmedBidCl.root.visibility = View.VISIBLE
              binding.cardInput.confirmedBidCl.title = "Bid Confirmed for ₹"+DecimalFormat("#########").format(binding.transaction?.transactionBid?.bidAmount)
              binding.cardInput.confirmedBidCl.subTitle = "Provide the driver and vehicle details"
              binding.cardInput.confirmedBidCl.actionLabel = "Go To Placement Tab"
              binding.cardInput.confirmedBidCl.btnAction.setOnClickListener {
                startActivity(homeActivityIntent("placement", this@BidDetailsActivity))

                // fragmentAction(NavigateHomeFragmentAction(HomeFragmentType.PlacementsFragment))
              }
              val data = viewModel.transaction as HomeBidsRequestItemData
              if (data.clientConfirmationPending == false) {
                binding.bottomLay.visibility = View.GONE
              } else {
                binding.bottomLay.visibility = View.VISIBLE
                binding.btnTripView.setOnClickListener {
                  startActivity(
                    tripDetailsIntent(
                      viewModel.transaction.uuid.toString(), this@BidDetailsActivity
                    )
                  )
                }

              }
            }

           /* ViewBidDetailsConfirmedBidBinding.inflate(
              layoutInflater, binding.containerActions, false
            )
              .apply {
                binding.timerLayout.visibility = View.GONE
                pickUpLocation =
                  StringUtils.capitalize(state.pickupLocation)
                    ?: getString(string.not_available)
                vehicleNumber = state.vehicleNumber ?: getString(string.not_available)
                driverPhone =
                  state.driverDetails?.driverPhoneNo ?: getString(string.not_available)
                if (viewModel.refreshCalled == false)
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_SOURCE, PROPERTY_SUB_SOURCE),
                    mutableListOf(viewModel.transactionId, source, subSource)
                  )

                binding.status.visibility = View.VISIBLE
                val data = viewModel.transaction as HomeBidsRequestItemData

                if (data.clientConfirmationPending == false) {
                  binding.status.text = resources.getString(R.string.label_pending)
                  binding.status.setTextColor(ContextCompat.getColor(this@BidDetailsActivity, R.color.pending))
                  binding.bottomLay.visibility = View.GONE
                } else {
                  binding.status.text = resources.getString(R.string.label_confirm)
                  binding.buttonConfirm.text = "View Trip"
                  binding.status.setBackground(
                    ContextCompat.getDrawable(this@BidDetailsActivity,
                      R.drawable.bg_all_round_corner_light_green_12
                    )
                  )
                  binding.status.setTextColor(ContextCompat.getColor(this@BidDetailsActivity, R.color.bid_placed_green))
                  binding.bidLay.visibility = View.GONE
                  binding.numLay.visibility = View.GONE
                  binding.bottomLay.visibility = View.VISIBLE
                  binding.buttonConfirm.setOnClickListener {
                    startActivity(
                      tripDetailsIntent(
                        viewModel.transaction.uuid.toString(), this@BidDetailsActivity
                      )
                    )
                  }

                }
              }*/
          }
          is BidDetailsUserBidState_RejectedBid -> {
            binding.mainCl.visibility = View.VISIBLE
            Log.d("called","BidDetailsUserBidState_RejectedBid")
            binding.cardInput.root.visibility = View.VISIBLE
            binding.cardInput.editBidCl.visibility = View.GONE
            binding.cardInput.rejectedBidCl.root.visibility = View.VISIBLE
            binding.cardInput.rejectedBidCl.title = "Bid not selected"
              try{
                binding.cardInput.rejectedBidCl.subTitle = "Winning bid price is ₹${DecimalFormat("#########").format(state.acceptedBid?.bidAmount?.toInt())}"
              }catch (e:Exception){
              }
            binding.cardInput.rejectedBidCl.actionLabel = "Explore New Bids"
            binding.cardInput.rejectedBidCl.btnAction.setOnClickListener {
           //   homeActivityIntent(HomeFragmentType.LoadsTruckFragment.title,this@BidDetailsActivity)
              startActivity(homeActivityIntent("load", this@BidDetailsActivity))

              // fragmentAction(NavigateHomeFragmentAction(HomeFragmentType.PlacementsFragment))
            }
          /*  ViewBidDetailsRejectedBidBinding.inflate(
              layoutInflater, binding.containerActions, false
            )
              .apply {
                binding.timerLayout.visibility = View.GONE
                binding.bottomLay.visibility = View.GONE
                val bidText = getString(string.msg_your_bid) + if (state.isPMTIndent) {
                  StringUtils.formatAmount(state.userBid.pmtRate ?: 0.0) + "/MT"
                } else {
                  StringUtils.formatAmount(state.userBid.bidAmount)
                }
                textUserHighestBid.text = bidText
                if (viewModel.refreshCalled == false)
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_SOURCE, PROPERTY_SUB_SOURCE),
                    mutableListOf(viewModel.transactionId, source, subSource)
                  )
                binding.status.visibility = View.VISIBLE
                binding.status.text = resources.getString(R.string.label_lost)
                binding.textTargetPrice.text = "₹" + if (state.isPMTIndent) {
                  StringUtils.formatAmount(state.userBid.pmtRate ?: 0.0)
                } else {
                  StringUtils.formatAmount(state.userBid.bidAmount)
                }
                binding.textTargetPriceLabel.text = "Your Bid"
                binding.priceLay.visibility = View.VISIBLE
                binding.status.setBackgroundColor(ContextCompat.getColor(this@BidDetailsActivity, R.color.status_lost_bg))
                binding.status.setTextColor(ContextCompat.getColor(this@BidDetailsActivity, R.color.status_lost_bid))
                binding.bottomLay.visibility = View.GONE
              }*/
          }
          is BidDetailsUserBidState_CancelledBid -> {
            binding.mainCl.visibility = View.VISIBLE
            Log.d("called","BidDetailsUserBidState_CancelledBid")
            binding.cardInput.root.visibility = View.VISIBLE

            binding.cardInput.editBidCl.visibility = View.GONE
            binding.cardInput.rejectedBidCl.root.visibility = View.VISIBLE
            binding.cardInput.rejectedBidCl.title = "Demand cancelled"
            binding.cardInput.rejectedBidCl.subTitle = "The demand was cancelled by the client"
            binding.cardInput.rejectedBidCl.actionLabel = "Explore New Bids"
            binding.cardInput.rejectedBidCl.btnAction.setOnClickListener {
             // homeActivityIntent(HomeFragmentType.LoadsTruckFragment.title,this@BidDetailsActivity)
              startActivity(homeActivityIntent("load", this@BidDetailsActivity))
              //action(NavigateHomeFragmentAction(HomeFragmentType.LoadsTruckFragment))
            }
           /* ViewBidDetailsCancelledBidBinding.inflate(
              layoutInflater, binding.containerActions, false
            )
              .apply {
                binding.timerLayout.visibility = View.GONE
                binding.bottomLay.visibility = View.GONE
                binding.bidLay.visibility = View.GONE
                val bidText = getString(string.msg_your_bid) + if (state.isPMTIndent) {
                  StringUtils.formatAmount(state.userBid.bidAmount ?: 0.0) + "/MT"
                } else {
                  StringUtils.formatAmount(state.userBid.bidAmount)
                }
                if (viewModel.refreshCalled == false)
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_SOURCE, PROPERTY_SUB_SOURCE),
                    mutableListOf(viewModel.transactionId, source, subSource)
                  )
                textUserHighestBid.text = bidText
                binding.status.visibility = View.VISIBLE
                binding.status.text = resources.getString(R.string.label_cancel)
                binding.textTargetPrice.text = "₹" + if (state.isPMTIndent) {
                  StringUtils.formatAmount(state.userBid.pmtRate ?: 0.0)
                } else {
                  StringUtils.formatAmount(state.userBid.bidAmount)
                }
                binding.textTargetPriceLabel.text = "Your Bid"
                binding.priceLay.visibility = View.VISIBLE
                binding.status.setTextColor(ContextCompat.getColor(this@BidDetailsActivity, R.color.status_lost))
                binding.bottomLay.visibility = View.GONE
              }*/
          }

          is BidDetailsUserBidState_BulkLoad_Edit -> {
          /*  ViewBidDetailsBulkLoadEditBinding.inflate(
           //   layoutInflater, binding.containerActions, false
            )
              .apply {
            //    binding.timerLayout.visibility = View.GONE

                val data = viewModel.transaction as HomeBidsRequestItemData
                var bidAmount = ""
                var expectedArrivalPickup = ""
                if (data.transactionStatus == "cancelled") {
                  btnReviseBidInsider.visibility = View.GONE
                  reduceText.visibility = View.GONE
                }
                data.bulkTransactionBids = state.bids
                bidsRecieved = state.bidsCount
                data.let {
                  if (!it.bulkTransactionBids.isNullOrEmpty()) {
                    if (viewModel.restrictEventTrigger && viewModel.refreshCalled == false) {
                      analyticsUtil.moEngageTrackEvent(
                        EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                        mutableListOf(
                          PROPERTY_ORDER_ID,
                          PROPERTY_BID_COUNT,
                          PROPERTY_SOURCE,
                          PROPERTY_SUB_SOURCE
                        ),
                        mutableListOf(
                          viewModel.transactionId,
                          bidsRecieved.toString(),
                          source,
                          subSource
                        )
                      )
                    }
                  } else {
                    if (viewModel.restrictEventTrigger && viewModel.refreshCalled == false)
                      analyticsUtil.moEngageTrackEvent(
                        EVENT_PAGE_LOAD_ORDER_DETAILS_WITHOUT_EXISTING_BID,
                        mutableListOf(PROPERTY_ORDER_ID, PROPERTY_SOURCE, PROPERTY_SUB_SOURCE),
                        mutableListOf(viewModel.transactionId, source, subSource)
                      )
                  }
                }
                data.let {
                  if (!it.bulkTransactionBids.isNullOrEmpty()) {
                    for (transactionBid in it.bulkTransactionBids) {
                      if (bidAmount.isNullOrEmpty()) {
                        bidAmount = transactionBid.bidAmount.toString()
                      } else {
                        bidAmount = bidAmount + "," + transactionBid.bidAmount.toString()
                      }
                      if (expectedArrivalPickup.isNullOrEmpty()) {
                        expectedArrivalPickup =
                          transactionBid.expectedArrivalTimePickupRemark.toString()
                      } else {
                        expectedArrivalPickup =
                          expectedArrivalPickup + transactionBid.expectedArrivalTimePickupRemark.toString()
                      }
                    }
                  }
                }

           //     binding.bidLay.visibility = View.GONE
            //    binding.bottomLay.visibility = View.GONE

                rvBidSummary.apply {
                  layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                  adapter = this@BidDetailsActivity.adapter
                  (adapter as BulkBidsRVAdapter).clearItems()
                  viewModel.getUserBulkBids(
                    state.bids, state.lowestAndUserBidPair.second.let { it!!.bidAmount })

                }

                btnReviseBidInsider.setOnClickListener {
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_NOT_LOWEST_BID_CTA,
                    mutableListOf(PROPERTY_ORDER_ID),
                    mutableListOf(data.uuid.toString())
                  )
                //  bidDialog()
                }
                if ((!viewModel.restrictEventTrigger && !viewModel.refreshCalled) || isFirstBid) {
                  isFirstBid = false
                  if (!reviseInitiated) {
                    analyticsUtil.moEngageTrackEvent(
                      EVENT_ORDER_DETAILS_BID_SUBMIT,
                      mutableListOf(
                        PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_USER_BID_VALUE,
                        PROPERTY_VEHICLE_REPORTING_DATE_TIME,
                        PROPERTY_SOURCE, PROPERTY_SUB_SOURCE
                      ),
                      mutableListOf(
                        state.lowestAndUserBidPair.second?.transactionId ?: "",
                        state.bidsCount.toString(),
                        bidAmount,
                        expectedArrivalPickup,
                        source, subSource
                      )
                    )
                  } else {
                    analyticsUtil.moEngageTrackEvent(
                      EVENT_BID_REVISE_SUBMITTED,
                      mutableListOf(
                        PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE,
                        PROPERTY_USER_BID_VALUE_OLD, PROPERTY_USER_BID_VALUE_NEW,
                        PROPERTY_SOURCE,
                        PROPERTY_SUB_SOURCE
                      ),
                      mutableListOf(
                        state.lowestAndUserBidPair.second?.transactionId ?: "",
                        state.bidsCount.toString() ?: "",
                        state.lowestAndUserBidPair?.first?.bidAmount?.toString() ?: " ",
                        oldAmountbids,
                        bidAmount,
                        source,
                        subSource
                      )
                    )
                    reviseInitiated = false
                  }
                } else {
                  viewModel.refreshCalled = false
                }
                viewModel.restrictEventTrigger = false
              }*/
          }
          else -> null
        }?.let { _binding ->
          /* bidding ended */
//          binding.textBidEnded.visible(_binding is ViewBidDetailsRejectedBidBinding)
         /* binding.containerActions.apply {
            removeAllViews()
            addView(_binding.root)
          }*/
        }
      }
    }
  }

  override fun onBackPressed() {
    userPrefs.setPreviousScreen(this.javaClass.name)
    super.onBackPressed()
  }

  /**
   * Create/edit bid dialog
   */
//  private fun bidDialog(bid: TransactionBid? = null) {
//    when (viewModel.userPrefs.canBid()) {
//      APPROVED -> {
//        binding.transaction?.let {
//          if (viewModel.dmtStatus == "dmt" || it.isDMTIndent()) {
//            uiUtils.showProgress()
//            viewModel.fetchTruckType(it);
//          } else {
//            if (it.transactionBid == null) {
//              analyticsUtil.moEngageTrackEvent(
//                EVENT_ORDER_DETAILS_BID_INITIATE,
//                mutableListOf(
//                  PROPERTY_ORDER_ID,
//                  PROPERTY_BID_COUNT,
//                  PROPERTY_SOURCE,
//                  PROPERTY_SUB_SOURCE
//                ),
//                mutableListOf(it?.uuid ?: "", viewModel.bidCount.toString(), source, subSource)
//              )
//              reviseInitiated = false
//            } else {
//              analyticsUtil.moEngageTrackEvent(
//                EVENT_BID_REVISE_INITIATED,
//                mutableListOf(
//                  PROPERTY_ORDER_ID,
//                  PROPERTY_BID_COUNT,
//                  PROPERTY_ORDER_LOWEST_BID_VALUE,
//                  PROPERTY_SOURCE,
//                  PROPERTY_SUB_SOURCE
//                ),
//                mutableListOf(
//                  it?.uuid.toString(), viewModel.bidCount.toString(),
//                  viewModel.lowestBid.toString(), source, subSource
//                )
//              )
//              reviseInitiated = true
//            }
//            BidDetailsCreateEditDialog(
//              this, it, bid, viewModel, analyticsUtil = analyticsUtil, userPrefs = userPrefs,
//              fromPage = "load_detail"
//            ).show()
//          }
//        }
//      }
//      UNAPPROVED -> {
//        dialogUtils.showBasicConfirmDialog(
//            string.title_dialog_supplier_not_approved,
//            string.msg_dialog_supplier_not_approved,
//            getString(string.label_call_us), getString(string.label_mail_us),
//            { callHelpline() }, { sendMail() }
//        )
//      }
//      DISABLED -> {
//        dialogUtils.showBasicConfirmDialog(
//            string.title_dialog_supplier_disabled,
//            string.msg_dialog_supplier_disabled,
//            getString(string.label_call_us), getString(string.label_mail_us),
//            { callHelpline() }, { sendMail() }
//        )
//      }
//    }
//  }

  override fun handleAction(
    actionId: String,
    position: Int,
    item: BaseBulkBidSummaryRVAdapterItem<*>
  ) {
    when(actionId){
      EXPAND_CARD -> {
        val bidData = item.data as BulkBidSummaryItemData
        bidData.expanded = !bidData.expanded
        BidDetailsViewModel.truckNumTextViewAdded = !BidDetailsViewModel.truckNumTextViewAdded
        adapter.notifyItemChanged(position)
      }
      OPEN_CONFIRMED_BID -> {

      }
    }
  }

  

  fun placementInput() {
    binding.cardInput.placementCl.editAutoCompleteTrucks.visibility = View.GONE
    binding.cardInput.placementCl.driverNameError.visibility = View.GONE
    if(homePlacementsItemData?.loadType==LoadTypes.intracityAdhoc.name || homePlacementsItemData?.loadType==LoadTypes.intracityRegular.name){
      viewModel.getFacilityAddress(homePlacementsItemData?.originCenterCode)
      binding.cardInput.routeAddress.visibility = View.VISIBLE
      binding.cardInput.mapText.visibility = View.VISIBLE
      binding.cardInput.mapText.setOnClickListener {
        navigateToMap()
      }
    }else{
      binding.cardInput.routeAddress.visibility = View.GONE
      binding.cardInput.mapText.visibility = View.GONE

    }
    viewModel.addressLiveData.observe(this, Observer {
      binding.cardInput.routeAddress.text = it.propertyAddressDetails?.address
    })
    viewModel.updateVehicleDetails.observe(this, Observer {
      if(it){
        uiUtils.hideProgress()
        REFRESH_ON_BACK_PLACEMENT = true
        when(homePlacementsItemData?.loadType){
          LoadTypes.intracityAdhoc.name -> showIntracityAdhocSuccessDialog()
          else -> showSuccessEditDialog("Submitted successfully!")
        }
      }else{
        uiUtils.hideProgress()
      }
    })
    
    autoCompleteUtils.autoCompleteTruck(binding.cardInput.placementCl.editAutoCompleteTrucks){
      // Ensure UI operations run on main thread
      binding.cardInput.placementCl.editAutoCompleteTrucks.post {
        if(it=="Add New Truck"){
          // Existing input is preserved by DelhiveryTrucksAutoEditText
          this?.let {showAddTruckBottomSheet()}
        }else if(validateTruckNumber(it)){
          isValidVehicleNumber = true
          binding.cardInput.placementCl.vehicleError.visibility = View.GONE
          binding.cardInput.placementCl.editTextVehicleNumber.text = it
          binding.cardInput.placementCl.editAutoCompleteTrucks.visibility = View.GONE
          binding.cardInput.placementCl.editTextVehicleNumber.visibility = View.VISIBLE
          // Don't call enableSubmitPlacement here - let TextWatcher handle it
        } else {
          binding.cardInput.placementCl.vehicleError.visibility = View.VISIBLE
          binding.cardInput.placementCl.vehicleError.text = "Please enter valid vehicle Number"
          isValidVehicleNumber = false
          // Don't call enableSubmitPlacement here - let TextWatcher handle it
        }
      }
    }
    
    binding.cardInput.placementCl.editTextVehicleNumber.setOnClickListener {
      binding.cardInput.placementCl.editTextVehicleNumber.visibility = View.GONE
      binding.cardInput.placementCl.editAutoCompleteTrucks.visibility = View.VISIBLE
      binding.cardInput.placementCl.editAutoCompleteTrucks.focusClick()
      isValidVehicleNumber = false
      enableSubmitPlacement()
    }
    
    binding.cardInput.placementCl.editTextVehicleNumber.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) = Unit
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
    
    // Driver name field click handling - now just focus the single field
    binding.cardInput.placementCl.editAutoCompleteDriverName.setOnClickListener {
      hasUserInteractedWithDriverName = true
      binding.cardInput.placementCl.editAutoCompleteDriverName.focusClick()
      enableSubmitPlacement()
    }
    
    // Simple TextWatcher for manual typing validation
    binding.cardInput.placementCl.editAutoCompleteDriverName.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) = Unit
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val text = s?.toString() ?: ""
        if (text.length >= 2) {
          isValidDriverName = true
          binding.cardInput.placementCl.driverNameError.visibility = View.GONE
        } else {
          isValidDriverName = false
          if (hasUserInteractedWithDriverName && text.isNotEmpty()) {
            binding.cardInput.placementCl.driverNameError.visibility = View.VISIBLE
            binding.cardInput.placementCl.driverNameError.text = "Please enter valid driver name"
          } else {
            binding.cardInput.placementCl.driverNameError.visibility = View.GONE
          }
        }
        enableSubmitPlacement()
      }
    })
    
    // Text change handling is now managed by the AutoCompleteUtils
    
    homePlacementsItemData?.vehicleNumber?.let {
      Log.i("vehicleNumber", it)
      isValidVehicleNumber = true
      binding.cardInput.placementCl.editTextVehicleNumber.text = it
      binding.cardInput.placementCl.editAutoCompleteTrucks.setText(it)
      binding.cardInput.placementCl.editAutoCompleteTrucks.visibility = View.GONE
      binding.cardInput.placementCl.editTextVehicleNumber.visibility = View.VISIBLE
      enableSubmitPlacement()
    }
    
    homePlacementsItemData?.driverName?.let {
      Log.i("DriverNameValidation", "Initial setup: Found existing driver name: '${it}'")
      isValidDriverName = true
      binding.cardInput.placementCl.editAutoCompleteDriverName.setText(it)
      binding.cardInput.placementCl.driverNameError.visibility = View.GONE
      enableSubmitPlacement()
    }
    
    homePlacementsItemData?.driverPhone?.let {
      isValidDriverNumber = true
      binding.cardInput.placementCl.editDriverNumber.setText(it)
      enableSubmitPlacement()
    }
    
    if (homePlacementsItemData?.status=="Marked-in"){
      binding.cardInput.placementCl.editTextVehicleNumber.isEnabled = false
      binding.cardInput.placementCl.editAutoCompleteDriverName.isEnabled = false
      binding.cardInput.placementCl.editDriverNumber.isEnabled = false
      disableSubmitPlcButton()
    }
    
    binding.cardInput.placementCl.btnSubmit.setOnClickListener{
      submitPlacementDetails()
    }
    
    // SIMPLIFIED APPROACH - Use basic autocomplete without complex validation
    autoCompleteUtils.autoCompleteDriverNameWithPhone(
      binding.cardInput.placementCl.editAutoCompleteDriverName,
      { binding.cardInput.placementCl.editTextVehicleNumber.text.toString() }
    ){ driverData ->

    driverData.driverName?.let {
      isValidDriverName = it.length>=2
    }
    // Populate phone number when driver is selected from dropdown
    driverData.driverPhone?.let {
      binding.cardInput.placementCl.editDriverNumber.setText(it)
      isValidDriverNumber = validatePhoneNumber(it)
    }
    enableSubmitPlacement()
  }
    
    binding.cardInput.placementCl.editDriverNumber?.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) = Unit
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s != null && s.isNotEmpty() && s.isNotBlank()) {
          val input = s.trim().toString()
          if(input.length==10 && validatePhoneNumber(input)){
            isValidDriverNumber = true
          } else {
            isValidDriverNumber = false
          }
          // Don't call enableSubmitPlacement here - let TextWatcher handle it
        }else{
          isValidDriverNumber = false
          // Don't call enableSubmitPlacement here - let TextWatcher handle it
        }
      }
    })
    
    binding.cardInput.placementCl.btnContactPicker.setOnClickListener {
      val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
      startActivityForResult(pickContactIntent, REQCODE_PICK_CONTACT)
    }
  }
  private fun navigateToMap(){
    try {
      val gmmIntentUri = Uri.parse("geo:0,0?q=${homePlacementsItemData?.originCenterLat},${homePlacementsItemData?.originCenterLong}"+"(" + homePlacementsItemData?.originCenterName+ ")")
      val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
      mapIntent.setPackage("com.google.android.apps.maps")
      startActivity(mapIntent)
    } catch (e: Exception) {
      Toast.makeText(this, "Unable to open map", Toast.LENGTH_SHORT).show()
    }
  }
  private fun showAddTruckBottomSheet() {
    val dialog = AddTruckBottomSheetDialogFragment.newInstance(
      truckNumber= binding.cardInput.placementCl.editAutoCompleteTrucks.text?.toString()?:"",
      viewModelFactory,
      userPrefs,
      autoCompleteUtils,
      onTruckAdded = { truckNumber ->
        showSuccessEditDialog("Truck added successfully!")
        binding.cardInput.placementCl.editTextVehicleNumber.text = truckNumber
        binding.cardInput.placementCl.editAutoCompleteTrucks.visibility = View.GONE
        binding.cardInput.placementCl.editTextVehicleNumber.visibility = View.VISIBLE
        isValidVehicleNumber = true
        // Don't call enableSubmitPlacement here - let TextWatcher handle it
      }
    )
    dialog.show(supportFragmentManager, "AddTruckBottomSheetDialogFragment")
  }

  private fun  enableSubmitPlacement(){
    // DEBOUNCE PROTECTION - Prevent rapid calls to enableSubmitPlacement (reduced from 500ms to 100ms)
    /* val currentTime = System.currentTimeMillis()
     val timeSinceLastCall = currentTime - lastEnableSubmitTime
     if (timeSinceLastCall < 100) { // Less than 100ms since last call
       Log.i("validate", "Skipping enableSubmitPlacement - too frequent (${timeSinceLastCall}ms since last)")
       return
     }
     lastEnableSubmitTime = currentTime*/

    // Re-validate all conditions
    isValidVehicleNumber = validateTruckNumber(binding.cardInput.placementCl.editTextVehicleNumber.text.toString())

    // Re-validate driver name
    val driverNameText = binding.cardInput.placementCl.editAutoCompleteDriverName.text.toString()
    isValidDriverName = driverNameText.length >= 2

    // Re-validate driver phone
    val driverPhoneText = binding.cardInput.placementCl.editDriverNumber.text.toString()
    isValidDriverNumber = driverPhoneText.length == 10 && validatePhoneNumber(driverPhoneText)

    val errorVisibility = if(binding.cardInput.placementCl.driverNameError.visibility == View.VISIBLE) "VISIBLE" else "GONE"
    Log.i("validate", "DriverName: '$driverNameText', length: ${driverNameText.length}, isValidDriverName: $isValidDriverName, isValidVehicleNumber: $isValidVehicleNumber, isValidDriverNumber: $isValidDriverNumber, hasUserInteracted: $hasUserInteractedWithDriverName, errorVisibility: $errorVisibility")

    val isMarkedIn = homePlacementsItemData?.status == "Marked-in"
    val allValid = isValidDriverName && isValidVehicleNumber && isValidDriverNumber && !isMarkedIn

    Log.i("validate", "All conditions: driverName=$isValidDriverName, vehicle=$isValidVehicleNumber, phone=$isValidDriverNumber, markedIn=$isMarkedIn, allValid=$allValid")

    if(allValid){
      enableSubmitPlcButton()
    }else{
      disableSubmitPlcButton()
    }
  }

  private fun disableSubmitPlcButton(){
    binding.cardInput.placementCl.btnSubmit.isEnabled = false
  }

  private fun enableSubmitPlcButton(){
    binding.cardInput.placementCl.btnSubmit.isEnabled = true
  }

  private fun validateTruckNumber(number: String): Boolean{
    val pattern = Pattern.compile(
      "^[a-zA-Z]{2}(((0?[1-9]{1}|[1-9]{1}[0-9]{1})[a-zA-Z]{1,3})|(0[1-9]{1}|[1-9]{1}[0-9]{1}))[0-9]{4}$|^[a-zA-Z]{3}[0-9]{4}$"
    )
    return pattern.matcher(number).matches()
  }

  private fun validatePhoneNumber(number:String):Boolean{
    val sameDigitsReg = "^([0-9])\\1*$"
    var result = false
    val sameDigitsPattern = Pattern.compile(sameDigitsReg)
    if(!sameDigitsPattern.matcher(number).matches()){
      val standardNumberPattern = Pattern.compile("^[6-9]{1}[0-9]{9}$")
      result = standardNumberPattern.matcher(number).matches()
    }
    return result
  }

  private fun submitPlacementDetails() {
    if (homePlacementsItemData == null) {
      uiUtils.showToast("Placement data not available")
      return
    }
    if (binding.cardInput.placementCl.editAutoCompleteTrucks.visibility == View.VISIBLE) {
      binding.cardInput.placementCl.vehicleError.visibility = View.VISIBLE
      binding.cardInput.placementCl.vehicleError.text = "Please select a valid vehicle number"
      isValidVehicleNumber = false
      binding.cardInput.placementCl.editAutoCompleteTrucks.errorAnimate()
      enableSubmitPlacement()
    } else {
      binding.cardInput.placementCl.vehicleError.visibility = View.GONE
      
      try {
        val isOrionType = homePlacementsItemData?.loadType?.toLowerCase()?.contains("orion") ?: false
        
        val vehicleType = if (isOrionType ) {null} else if (homePlacementsItemData?.loadType?.toLowerCase()?.contains("adhoc") ?: false) {
          "adhoc"
        } else {
          "regular"
        }
        val contractType = if (isOrionType ) {null} else if  (homePlacementsItemData?.loadType!!.toLowerCase().contains("ftl")) {
          "ftl"
        } else {
          "intracity"
        }
        val action = if(isOrionType) {
          "placement_on_orion"
        } else if (homePlacementsItemData?.vehicleNumber == null) {
          "add_placement"
        } else {
          "update_placement"
        }
        try {
          uiUtils.showProgress()
          val updateVehicleDetailsRequest = UpdateVehicleDetailsRequest(
            binding.cardInput.placementCl.editTextVehicleNumber.text.toString(), 
            binding.cardInput.placementCl.editAutoCompleteDriverName.text.toString(), 
            binding.cardInput.placementCl.editDriverNumber.text.toString(), 
            contractType, 
            vehicleType, 
            homePlacementsItemData?.vehicleType ?: "", 
            homePlacementsItemData?.transporterSupplierId ?: "", 
            if(isOrionType) null else homePlacementsItemData?.contractId, 
            homePlacementsItemData?.transporterId ?: 0, 
            action, 
            if(isOrionType) null else homePlacementsItemData?.reportingTime ?: "", 
            if(isOrionType)null else homePlacementsItemData?.originCenterCode ?: "",  
            if(isOrionType)null else homePlacementsItemData?.vehicleNumber, 
            if(isOrionType)null else homePlacementsItemData?.vehicleId,  
            if(isOrionType)null else homePlacementsItemData?.driverName,  
            if(isOrionType)null else homePlacementsItemData?.driverPhone, 
            homePlacementsItemData?.transactionId
          )
          viewModel.updateVehicleDetails(updateVehicleDetailsRequest)
        }catch (e:Exception){
          uiUtils.showToast("Something went wrong")
          uiUtils.hideProgress()
        }
      } catch (e: IllegalArgumentException) {
        uiUtils.hideProgress()
        Log.e("PlacementDetails", e.toString())
      }
    }
  }

  private fun showIntracityAdhocSuccessDialog(){
    val title = getString(R.string.title_dialog_success)
    val subTittle = getString(R.string.sub_title_dialog_success)
    val playStoreLink = getString(R.string.driver_app_link)
    val hindiVideoLink = getString(R.string.hindi_video_link)
    val englishVideoLink = getString(R.string.english_video_link)
    
    val placementData = homePlacementsItemData
    Log.d("placementData=====>>>>", Gson().toJson(placementData))
    val ticketId = placementData?.transactionId ?: ""
    val reportingCentre = generateReportingCentreLink(placementData!!)
    val reportingTime = formatReportingTime(placementData?.reportingTime)
    
    dialogUtils.showDetailsSubmittedSuccessDialog(
      title = title,
      subTittle = subTittle,
      playStoreLink = playStoreLink,
      ticketId = ticketId,
      reportingCentre = reportingCentre,
      reportingTime = reportingTime,
      hindiVideoLink = hindiVideoLink,
      englishVideoLink = englishVideoLink,
      dialogInterface = object : DetailsSubmittedSuccessInterface {
        override fun onDialogDismissed() {
          REFRESH_ON_BACK_PLACEMENT = true
        /*  Log.d("onDialogDismissed===>>>","Dialog dismissed")
          Handler(Looper.myLooper()!!).postDelayed({
            REFRESH_ON_BACK_PLACEMENT = true
            finish()
          }, 100)*/
        }
      }
    )
  }

  private fun generateReportingCentreLink(placementData: HomePlacementsItemData): String {
    return if (placementData.originCenterLat != null && placementData.originCenterLong != null) {
      "https://maps.google.com/?q=${placementData.originCenterLat},${placementData.originCenterLong}"
    } else {
      val location = placementData.originCenterName ?: ""
      if (location.isNotEmpty()) {
        "https://maps.google.com/?q=$location"
      } else {
        ""
      }
    }
  }

  private fun formatReportingTime(reportingTime: String?): String {
    return if (!reportingTime.isNullOrEmpty()) {
      try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
        val outputFormat = java.text.SimpleDateFormat("dd MMM, hh:mm a")
        val date = inputFormat.parse(reportingTime)
        outputFormat.format(date ?: java.util.Date())
      } catch (e: Exception) {
        "--:--"
      }
    } else {
      "--:--"
    }
  }

  private fun showSuccessEditDialog(msg:String){
    val dialog = Dialog(this)
    val bindingDialog = DialogPlacementDetailsEditBinding.inflate(layoutInflater)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(bindingDialog.root)
    bindingDialog.titleText.text = msg
    bindingDialog.closeBtn.setOnClickListener {
      dialog.dismiss()
    }
    if(!this.isFinishing)
      dialog.show()
//    if(!this.isFinishing)
//      Handler(Looper.myLooper()!!).postDelayed({
//        dialog.dismiss()
//        REFRESH_ON_BACK_PLACEMENT = true
//        finish()
//      }, 2000)
    dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
    dialog.window?.setGravity(Gravity.BOTTOM)

  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      REQCODE_PICK_CONTACT -> {
        if (resultCode == RESULT_OK && data != null) {
          val contactUri: Uri? = data.data
          contactUri?.let {
            val cursor: Cursor? = contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
              if (c.moveToFirst()) {
                val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (numberIndex != -1) {
                  val phoneNumber = c.getString(numberIndex)
                  val trimmedNumber = phoneNumber?.replace("+91", "")?.replaceFirst("^0+".toRegex(), "")?.trim()
                  binding.cardInput.placementCl.editDriverNumber.setText(trimmedNumber)
                }
              }
            }
          }
        }
      }
    }
  }override fun bidPlacedSuccess(success: Boolean) {
    if(success)
      startActivity(homeActivityIntent("load", this@BidDetailsActivity))

  }

}

/* intent keys */
private const val TransactionIdIntentKey = "transaction_id"

/* intent keys */
private const val RequestTypeIntentKey = "request_type"
private const val FromPage = "from_page"
private const val ActiveBid= "active_bid"
private const val ForPlacementKey= "for_placement"
private const val PlacementData = "placement_data"

/**
 * Bid details intent
 */
fun bidDetailsIntent(
        transactionId: String,
        context: Context,
        requestType:String?=null,
        fromBidsPage:Boolean = false,
        active:Boolean = false,
        source:String?= VALUE_APP_FLOW,
        subSource:String?="NA",
        forPlacement:Boolean = false,
        homePlacementsItemData: HomePlacementsItemData? = null
) = Intent(context, BidDetailsActivity::class.java).apply {
  putExtra(TransactionIdIntentKey, transactionId)
  if(requestType!=null)
    putExtra(RequestTypeIntentKey, requestType)
  putExtra(FromPage,fromBidsPage)
  putExtra(ActiveBid,active)
  putExtra(PROPERTY_SOURCE,source)
  putExtra(PROPERTY_SUB_SOURCE,subSource)
  putExtra(ForPlacementKey,forPlacement)
  putExtra(PlacementData, homePlacementsItemData)
}