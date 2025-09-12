package com.delhivery.axle.ui.contractDetails

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.RequestOptions
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.repository.RequestType
import com.delhivery.axle.api.repository.TransactionStatus
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Cancelled
import com.delhivery.axle.data.bids.TransactionBidStatus.Open
import com.delhivery.axle.data.bids.TransactionBidStatus.Rejected
import com.delhivery.axle.data.home.bids.HaltCenters
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.bids.PaymentSlabs
import com.delhivery.axle.data.home.bids.SecondaryReportingCenters
import com.delhivery.axle.databinding.ActivityContractDetailsBinding
import com.delhivery.axle.databinding.DialogContractsBidSuccessBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.biddetails.BidDetailsContractCancelled
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_ContractResult
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_EditBid
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_LoadingBids
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_PlaceBid
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_PlaceBidFirst
import com.delhivery.axle.ui.bids.BidType
import com.delhivery.axle.ui.bids.userBidsIntent
import com.delhivery.axle.ui.home.activity.home.homeActivityIntent
import com.delhivery.axle.ui.home.fragments.contracts.REFRESH_ON_BACK
import com.delhivery.axle.utils.BidSuccessInterface
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.EVENT_ADD_TRUCK_INITIATE
import com.delhivery.axle.utils.EVENT_HOME_CONTRACT_CARD_CLICK
import com.delhivery.axle.utils.EVENT_REVISE_CONTRACT_BID
import com.delhivery.axle.utils.EVENT_SUBMIT_CONTRACT_BID
import com.delhivery.axle.utils.PROPERTY_BID_AMOUNT_DIFF
import com.delhivery.axle.utils.PROPERTY_CONTRACT_TYPE
import com.delhivery.axle.utils.PROPERTY_IS_FLEXIBLE
import com.delhivery.axle.utils.PROPERTY_ORDER_ID
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_SOURCE
import com.delhivery.axle.utils.PROPERTY_STATUS
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.StringUtils.capitalize
import com.delhivery.axle.utils.VALUE_APP_FLOW
import com.delhivery.axle.utils.VALUE_BANNER
import com.delhivery.axle.utils.prefs.APPROVED
import com.delhivery.axle.utils.prefs.DISABLED
import com.delhivery.axle.utils.prefs.UNAPPROVED
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.math.abs

class ContractDetailsActivity: BaseActivity<ActivityContractDetailsBinding, ContractDetailsViewModel>(),BidSuccessInterface {

  init {
    hasInlineProgress = true
  }
  @Inject lateinit var userPrefs: UserPrefs
  var routesArray:ArrayList<HaltCenters> = ArrayList()
  var flexibleReportingCentersArray:ArrayList<SecondaryReportingCenters> = ArrayList()
  var paymentSlabsArray:ArrayList<PaymentSlabs> = ArrayList()
  var source= VALUE_APP_FLOW
  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true
  private var amount = 0
  private var pmtRate = 0
  private var isChecked = false
  private var isValidBidAmount = false
  private var isValidTripCommit = false
  private var isValidVehicleNumber = false
  private var isValidPlacementDays = false
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("ContractDetailsActivity_SetupTime")
    activitySetupTrace?.start()
    try {
      require(
        !(intent == null || !intent.hasExtra(TransactionIdIntentKey))
      ) { "Required data $TransactionIdIntentKey not found" }
    } catch (e: Exception) {
      finish()
    }

    /* set transaction id */
    viewModel.transactionId = intent.getStringExtra(TransactionIdIntentKey) ?: ""
    viewModel.requestType = RequestType.Contract.type
    source = intent.getStringExtra(PROPERTY_SOURCE) ?: VALUE_APP_FLOW

   /* binding.backArrow.setOnClickListener {
      onBackPressedDispatcher.onBackPressed()
    }*/

  }

  private fun setBiddingInput(transaction:HomeBidsRequestItemData) {

    val items= resources.getStringArray(R.array.placementDaysItems)

    val spinnerAdapter= object : ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, items) {

      override fun isEnabled(position: Int): Boolean {
        return position != 0
      }

      override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
      ): View {
        val view: TextView = super.getDropDownView(position, convertView, parent) as TextView
        if(position == 0) {
          view.setTextColor(Color.GRAY)
        }
        return view
      }

    }

    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    binding.cardInput.spinnerPlacementDays.adapter = spinnerAdapter
   //  set binding params
    binding.apply {
      binding.cardInput.request = transaction
      Log.i("transaction",transaction.key())
      transaction.transactionBid?.bidAmount?.let {
        isValidBidAmount= true
        showBidAmountPerDayTrip(it.toInt(),transaction)
       /* binding.title.text = "Revise Your Bid"
        binding.bidAmountLbl.text = "Revised Bid Amount"*/
        binding.cardInput.etBidAmount?.setText(DecimalFormat("#########").format(it))
        if(transaction.isPMTIndent()){
          pmtRate = it.toInt()
          amount = (it*transaction.requestedCapacityMg).toInt()
        }else{
          amount = it.toInt()
        }
      }
      transaction.transactionBid?.tentativeTripCount?.let {
        isValidTripCommit = true
        binding.cardInput.etTripNumber?.setText(DecimalFormat("#########").format(it))
      }
      transaction.transactionBid?.vehicleNumber?.let {
        isValidVehicleNumber = true
        binding.cardInput.etVehicleNumber?.setText(it)
      }
      transaction.transactionBid?.placementDays?.let {
        isValidPlacementDays = true
        if(it.equals("1-2 Days")) {
          binding.cardInput.spinnerPlacementDays.setSelection(1)
        }else if(it.equals("3-5 Days")) {
          binding.cardInput.spinnerPlacementDays.setSelection(2)
        }else if(it.equals("6-8 Days")) {
          binding.cardInput.spinnerPlacementDays.setSelection(3)
        }
      }
      enableSubmit(transaction)
    }




    binding.cardInput.etBidAmount?.addTextChangedListener(object : TextWatcher {
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
        if (s != null && s.isNotEmpty() && s.isNotBlank()) {
          try {
            val input = s.trim()
              .toString()
              .toInt()
            var lowerPercentage = 0
            var upperPercentage = 0
            if(transaction.isItLHContract()){
              lowerPercentage  = 30
              upperPercentage  = 20
            }else if(transaction.isItFRContract()){
              lowerPercentage  = 20
              upperPercentage  = 10
            }else if(transaction.isItIntraCityContract()){
              lowerPercentage  = 20
              upperPercentage  = 20
            }
            if (transaction.isPMTIndent()) {
              pmtRate = input
              amount = (input * transaction.requestedCapacityMg).toInt()
              if(amount<250){
                showInvalidBidAmountError()
              }else{
                if(transaction.targetPrice!=null&&(amount>(transaction.targetPrice!!+transaction.targetPrice!!*upperPercentage/100)|| amount<(transaction.targetPrice!!-transaction.targetPrice!!*lowerPercentage/100))){
                  showInvalidBidAmountError()
                }else{
                  removeInvalidBidAmountError(transaction,amount)
                }
              }
              enableSubmit(transaction)
            }else{
              amount = input
              if(amount<250){
                showInvalidBidAmountError()
              }else{
                if (transaction.targetPrice!=null&&(amount > (transaction.targetPrice!! + transaction.targetPrice!! * upperPercentage / 100) || amount <(transaction.targetPrice!! - transaction.targetPrice!! * lowerPercentage / 100))) {
                  showInvalidBidAmountError()
                } else {
                  if (transaction.transactionBid != null) {
                    if(transaction.isItIntraCityContract()){
                      if (abs(amount-transaction.transactionBid!!.bidAmount.toInt()) >=100 && abs(amount-transaction.transactionBid!!.bidAmount.toInt()) %100==0) {
                        removeInvalidBidAmountError(transaction,amount)
                      }else{
                        showInvalidBidAmountError("100")
                      }
                    }else{
                      if(amount<12000){
                        if (abs(amount-transaction.transactionBid!!.bidAmount.toInt()) >=50 && abs(amount-transaction.transactionBid!!.bidAmount.toInt()) %50==0) {
                          removeInvalidBidAmountError(transaction,amount)
                        }else{
                          showInvalidBidAmountError("50")
                        }
                      }else{
                        if (abs(amount-transaction.transactionBid!!.bidAmount.toInt()) >=250 && abs(amount-transaction.transactionBid!!.bidAmount.toInt()) %250==0) {
                          removeInvalidBidAmountError(transaction,amount)
                        }else{
                          showInvalidBidAmountError("250")
                        }
                      }
                    }
                  }else{
                    removeInvalidBidAmountError(transaction,amount)
                  }
                }

                enableSubmit(transaction)
              }
            }

          } catch (e: NumberFormatException) {
            amount = 0
          } catch (e: Exception) {
            amount = 0
          }
        }else{
          amount = 0
          isValidBidAmount = false
          enableSubmit(transaction)
        }
      }
    })

    binding.cardInput.etTripNumber?.addTextChangedListener(object : TextWatcher {
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
        if (s != null && s.isNotEmpty() && s.isNotBlank()) {
          val input = s.trim()
            .toString()
            .toInt()
          if(transaction.isItFRContract()) {
            if(input==0){
              binding.cardInput.tripError.visibility = View.VISIBLE
              binding.cardInput.tripError.text ="Invalid trip count"
              isValidTripCommit = false
            }else if (input > 0 && input > transaction.tentativeTripCount!!) {
              binding.cardInput.tripError.text =getString(R.string.exceeds_tentative_trip_count)
              binding.cardInput.tripError.visibility = View.VISIBLE
              isValidTripCommit = false

            } else {
              isValidTripCommit = true
              binding.cardInput.tripError.visibility = View.GONE

            }
            enableSubmit(transaction)
          }else{
            isValidTripCommit = true
            enableSubmit(transaction)
          }
        }else{
          if(transaction.isItFRContract()){
            isValidTripCommit = false
            enableSubmit(transaction)
          }
        }
      }
    })


    binding.cardInput.etVehicleNumber?.addTextChangedListener(object : TextWatcher {
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
        if (s != null && s.isNotEmpty() && s.isNotBlank()) {
          val input = s.trim()
            .toString()
          if(transaction.isItIntraCityContract()) {
            if(validateTruckNumber(input)){
              isValidVehicleNumber = true
              binding.cardInput.vehicleError.visibility = View.GONE
            } else {
              binding.cardInput.vehicleError.visibility = View.VISIBLE
              binding.cardInput.vehicleError.text ="Invalid Vehicle Number"
              isValidVehicleNumber = false

            }
            enableSubmit(transaction)
          }else{
            isValidVehicleNumber = true
            enableSubmit(transaction)
          }
        }else{
          if(transaction.isItIntraCityContract()){
            isValidVehicleNumber = false
            enableSubmit(transaction)
          }
        }
      }
    })




    binding.cardInput.spinnerPlacementDays.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
      override fun onNothingSelected(parent: AdapterView<*>?) {
      }

      override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
      ) {
        val value = parent!!.getItemAtPosition(position).toString()
        if(value == items[0]){
          (view as TextView).setTextColor(Color.GRAY)
          isValidPlacementDays = false
          enableSubmit(transaction)
        }else{
          isValidPlacementDays = true
          enableSubmit(transaction)
        }
      }

    }

    binding.cardInput.placeBidButton.setOnClickListener {
     // binding.editBidAmount.clearFocus()
      when (viewModel.userPrefs.canBid()) {
        APPROVED -> {
          submit(transaction)
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


    }

   // binding.buttonCancel.setOnClickListener { dismiss() }
  }

  private fun  enableSubmit(transaction:HomeBidsRequestItemData){
    if(transaction.isItLHContract()){
      isValidTripCommit = true
      isValidVehicleNumber=true
      isValidPlacementDays = true
    }else if (transaction.isItFRContract()){
      isValidVehicleNumber=true
      isValidPlacementDays = true
    }else if (transaction.isItIntraCityContract()){
      isValidTripCommit = true
    }
    if(isValidBidAmount&&isValidTripCommit&&isValidVehicleNumber&&isValidPlacementDays) {
      if (transaction.transactionBid != null) {
        // checking if value has changed
        if(checkValueChangesForLHFTL(transaction)||checkValueChangeForFRC(transaction)||checkValueChangesForIntraCity(transaction)) {
          disableSubmitButton()
        }else{
          enableSubmitButton()
        }
      } else {
        enableSubmitButton()
      }
    }else{
      disableSubmitButton()
    }
  }

  private fun validateTruckNumber(number: String): Boolean{
    val pattern = Pattern.compile(
      "^[a-zA-Z]{2}(((0?[1-9]{1}|[1-9]{1}[0-9]{1})[a-zA-Z]{1,3})|(0[1-9]{1}|[1-9]{1}[0-9]{1}))[0-9]{4}$|^[a-zA-Z]{3}[0-9]{4}$"
    )
    return pattern.matcher(number).matches()
  }

  private fun showInvalidBidAmountError(amount:String?=null){
    isValidBidAmount = false
    binding.cardInput.bidError.setTextColor(ContextCompat.getColor(this,R.color.bid_error))
    if(amount!=null){
      binding.cardInput.bidError.text = getString(string.bid_revise_error,amount)
    }else{
      binding.cardInput.bidError.text =  "Invalid bid: Amount outside the allowed range."
    }
    binding.cardInput.bidError.visibility = View.VISIBLE
  }

  private fun removeInvalidBidAmountError(transaction: HomeBidsRequestItemData, amount:Int){
    isValidBidAmount = true
    showBidAmountPerDayTrip(amount,transaction)
   // binding.cardInput.bidError.visibility = View.GONE
  }

  private fun showBidAmountPerDayTrip(bidAmount:Int, transaction: HomeBidsRequestItemData){
    try {
//      if(transaction.isItLHContract()){
//        binding.cardInput.bidError.setTextColor(ContextCompat.getColor(this,R.color.text_grey))
//        binding.cardInput.bidError.visibility = View.VISIBLE
//        val amount = bidAmount/((transaction.operatingDays!!/7)*30)
//        binding.cardInput.bidError.text = "Your bid price per day: ₹${StringUtils.formatDecimalAmount(
//          amount.toDouble()
//        )}"
//      }else
        if (transaction.isItIntraCityContract()) {
            binding.cardInput.bidError.setTextColor(ContextCompat.getColor(this,R.color.text_grey))
            binding.cardInput.bidError.visibility = View.VISIBLE
            val amount = bidAmount/transaction.intracityDays!!.toInt()
            binding.cardInput.bidError.text = "Your bid price per day: ₹${StringUtils.formatDecimalAmount(amount.toDouble())}"
        }else{
            binding.cardInput.bidError.visibility = View.GONE
        }
    } catch (e:Exception){
      Log.i("Error", e.toString())
    }

  }
  private fun checkValueChangeForFRC(transaction:HomeBidsRequestItemData): Boolean{
    return if (transaction.transactionBid != null) (transaction.isItFRContract()&&Integer.parseInt(binding.cardInput.etTripNumber.text.toString()) == viewModel.transaction.transactionBid!!.tentativeTripCount
            && if(transaction.isPMTIndent())amount == (transaction.transactionBid!!.bidAmount*transaction.requestedCapacityMg).toInt() else amount==viewModel.transaction.transactionBid!!.bidAmount.toInt())
    else false
  }

  private fun checkValueChangesForLHFTL(transaction:HomeBidsRequestItemData): Boolean{
    return if (transaction.transactionBid != null)(transaction.isItLHContract() && if(transaction.isPMTIndent())amount == (viewModel.transaction.transactionBid!!.bidAmount*viewModel.transaction.requestedCapacityMg).toInt() else amount==transaction.transactionBid!!.bidAmount.toInt())
    else false
  }

  private fun checkValueChangesForIntraCity(transaction:HomeBidsRequestItemData): Boolean{
    return if (transaction.transactionBid != null)(transaction.isItIntraCityContract() &&if(transaction.isPMTIndent())amount == (viewModel.transaction.transactionBid!!.bidAmount*viewModel.transaction.requestedCapacityMg).toInt() else amount==transaction.transactionBid!!.bidAmount.toInt() && binding.cardInput.etVehicleNumber.text.toString() == transaction.transactionBid!!.vehicleNumber && binding.cardInput.spinnerPlacementDays.selectedItem.toString() == transaction.transactionBid!!.placementDays)
    else false
  }

  private fun disableSubmitButton(){
    binding.cardInput.placeBidButton.isEnabled = false
  }
  private fun enableSubmitButton(){
    binding.cardInput.placeBidButton.isEnabled = true

  }
  private fun submit(transaction: HomeBidsRequestItemData) {
    try {
      uiUtils.showProgress()
      if (transaction.transactionBid == null) {
        analyticsUtil.moEngageTrackEvent(
          EVENT_SUBMIT_CONTRACT_BID,mutableListOf(PROPERTY_USER_ID,
            PROPERTY_PHONE_NO,
            PROPERTY_CONTRACT_TYPE, PROPERTY_STATUS,PROPERTY_ORDER_ID, PROPERTY_SOURCE,
            PROPERTY_IS_FLEXIBLE),
          mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:"",transaction.contractType?:"",viewModel.transaction.contractEventStatusText()?:"", transaction.key(),source,transaction.isFlexible.toString()))

        viewModel.createBid(
          transaction.isPMTIndent(), transaction.key(), amount, pmtRate,
          transaction.biddingType
            ?: "FTL", 0,if(binding.cardInput.etTripNumber.text.isNullOrEmpty())null else Integer.parseInt(binding.cardInput.etTripNumber.text.toString()),if(binding.cardInput.etVehicleNumber.text.isNullOrEmpty())null else binding.cardInput.etVehicleNumber.text.toString().uppercase(),if(transaction.isItIntraCityContract())binding.cardInput.spinnerPlacementDays.selectedItem.toString() else null
        )
      } else {
        analyticsUtil.moEngageTrackEvent(
          EVENT_REVISE_CONTRACT_BID,mutableListOf(PROPERTY_USER_ID,
            PROPERTY_PHONE_NO,
            PROPERTY_CONTRACT_TYPE, PROPERTY_STATUS, PROPERTY_ORDER_ID, PROPERTY_BID_AMOUNT_DIFF, PROPERTY_SOURCE,
            PROPERTY_IS_FLEXIBLE),
          mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:"",transaction.contractType?:"",transaction.contractEventStatusText()?:"", transaction.key(), (amount-transaction.transactionBid?.bidAmount!!).toString(),source,transaction.isFlexible.toString()))

        viewModel.editBid(
          viewModel.transaction.isPMTIndent(), transaction.key(), transaction.transactionBid!!.key(),
          amount, pmtRate, transaction.biddingType ?: "FTL", 0,if(binding.cardInput.etTripNumber.text.isNullOrEmpty())null else Integer.parseInt(binding.cardInput.etTripNumber.text.toString()),if(binding.cardInput.etVehicleNumber.text.isNullOrEmpty())null else binding.cardInput.etVehicleNumber.text.toString().uppercase(),if(transaction.isItIntraCityContract())binding.cardInput.spinnerPlacementDays.selectedItem.toString() else null
        )
      }

    } catch (e: IllegalArgumentException) {
        uiUtils.hideProgress()
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    title = ""//""Order ID - " + viewModel.transactionId
    /* setup live data observers */
    viewModel.progressLiveData.observe(this, ProgressObserver())
    viewModel.transactionLiveData.observe(this, TransactionObserver())
   viewModel.transactionBidLiveData.observe(this, TransactionBidObserver())
    viewModel.bidPriceLiveData.observe(this, Observer {
      if (it != null) {
        binding.transaction?.transactionBid = it
        val visibility =
          if (binding.transaction?.bidAmount()
              .isNullOrEmpty()
          ) View.GONE else View.VISIBLE
      }
    })
    viewModel.errorBiddingLiveData.observe(this) {
      uiUtils.hideProgress()
    }
    // show Success Bid Dialog
    viewModel.successBidLiveData.observe(this) {
      if (it.first) {
        uiUtils.hideProgress()
        val dialog = dialogUtils.showSuccessBidDialog(this,
          resources.getString(R.string.bid_placed_sucessfully),
          resources.getString(R.string.check_your_bid_status)
        )
        navigateToBid(dialog)

      }else if(it.second){
        uiUtils.hideProgress()
        val dialog =   dialogUtils.showSuccessBidDialog(this,
          resources.getString(R.string.bid_revised_sucessfully),
          resources.getString(R.string.check_your_bid_revise_status)
        )
        navigateToBid(dialog)

      }
    }
    viewModel.hideProgress.observe(this, Observer {
      if(it){
      uiUtils.hideProgress()
 //       binding.bottomLay.visibility = View.VISIBLE
      }
    })

    binding.refreshLayout.setOnRefreshListener {
      binding.mainCl.visibility = View.GONE
      refreshData()
    }
  /*  binding.buttonConfirm.setOnClickListener {
      bidDialog()
    }*/

    binding.containerError.btnAction.setOnClickListener {
      binding.mainCl.visibility = View.GONE
      refreshData()
    }

    //setBiddingInput()
    refreshData()
  }

  override fun onResume() {
    super.onResume()
    if (activitySetupTrace != null && isFirstResume) {
      activitySetupTrace?.stop()
      isFirstResume = false
    }
  }

  var countDownTimer: CountDownTimer? = null
  // timer for under 1 hrs slot
/* fun setTimer(bidEndingTime:String){

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        format.setTimeZone(TimeZone.getTimeZone("IST"));
        val date1: Date = format.parse(format.format(Date()))
        val date2: Date = format.parse(bidEndingTime)
        if (date2.compareTo(date1) > 0) {
          val mills: Long = date2.getTime() - date1.getTime()
          countDownTimer?.cancel()
          countDownTimer = object : CountDownTimer(mills, 1000) {
            override fun onTick(millisUntilFinished: Long) {
              try {
                val hours = (millisUntilFinished / (1000 * 60 * 60)).toInt()
                val mins = (millisUntilFinished / (1000 * 60)).toInt() % 60
                val secs = ((millisUntilFinished / 1000).toInt() % 60).toLong()
                val format = "%1$02d"
                val hrs = String.format(format, hours)
                val ms = String.format(format, mins)
                val sec = String.format(format, secs)
                val diff = ms + "m $sec" + "s"
                if (hours == 0) {
                  binding.tvBidTime.visibility = View.VISIBLE
                  binding.tvBidTime.setText("Closes in $diff")
                  binding.bidClosingStatusTime.visibility = View.VISIBLE
                  binding.bidClosingStatusTime.text = diff
                  binding.bidClosingStatusTime.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.destructive_red))
                }else  if(hours<=12){
                  binding.bidClosingStatusTime.visibility = View.VISIBLE
                  binding.bidClosingStatusTime.text = hrs+ " hrs"
                  binding.bidClosingStatusTime.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.heading_black))
                }else{
                  if(viewModel.transaction.transactionStatus==TransactionStatus.Cancelled.statusId){
                    binding.bidClosingStatusTime.visibility = View.VISIBLE
                  }else{
                    binding.bidClosingStatusTime.visibility = View.GONE
                    binding.bidClosingStatus.text = viewModel.transaction.bidEndTime()
                  }

                }

              } catch (e: Exception) {
                e.printStackTrace()
              }
            }

            override fun onFinish() {
              binding.tvBidTime.visibility = View.GONE
              binding.bottomLay.visibility = View.GONE
              refreshData()
            }
          }.start()
        } else {
          binding.tvBidTime.visibility = View.GONE
          binding.bottomLay.visibility = View.GONE
        }

 }*/

/*  private fun bidDialog(bid: TransactionBid? = null) {
    when (viewModel.userPrefs.canBid()) {
      APPROVED -> {
        binding.transaction?.let {
            ContractDetailsCreateEditDialog(
              this, it, bid, viewModel, analyticsUtil = analyticsUtil, userPrefs = userPrefs,
               source= source
            ).show()

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
  }*/

  fun enableKeyboard(){
    binding.cardInput.etBidAmount.requestFocus()

    // Delay to ensure the window is ready before showing the keyboard
    binding.cardInput.etBidAmount.post {
      val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      imm.showSoftInput(binding.cardInput.etBidAmount, InputMethodManager.SHOW_IMPLICIT)
    }
  }
  inner class ProgressObserver : Observer<Boolean> {
    override fun onChanged(t: Boolean) {
      t?.let {
        when (t) {
          true -> {
            binding.refreshLayout.isRefreshing = true
            binding.refreshing = true
          }
          false -> {
            binding.refreshLayout.isRefreshing = false
          }
        }
      }
      binding.executePendingBindings()
    }
  }

  /**
   * Transaction details and UI updation Observer
   */
  inner class TransactionObserver : Observer<HomeBidsRequestItemData> {
    override fun onChanged(t: HomeBidsRequestItemData) {

      if (t != null) {
        t.let { _transaction ->
          binding.error = false
          binding.transaction = _transaction
          binding.routeDetails.nonExpRouteDetails.visibility = t.isFRCContract()
          binding.routeDetails.routeDetails.visibility =  t.isLHContract()
          binding.routeDetails.intraCityRouteDetails.visibility =  t.isIntraCityFixedContract()
          binding.routeDetails.intraCityAdHocRouteDetails.visibility =  t.isIntraCityFlexibleContract()

          binding.vehicleDetails.visibility = View.VISIBLE

          // Capture event
          analyticsUtil.moEngageTrackEvent(
            EVENT_HOME_CONTRACT_CARD_CLICK,
            mutableListOf(
              PROPERTY_USER_ID,
              PROPERTY_PHONE_NO, PROPERTY_ORDER_ID, PROPERTY_STATUS, PROPERTY_CONTRACT_TYPE,
              PROPERTY_SOURCE, PROPERTY_IS_FLEXIBLE
            ),
            mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:"",
              _transaction.uuid ?: " ",_transaction.contractEventStatusText(),
              _transaction.contractType?:"",source,_transaction.isFlexible.toString()
            )
          )
          // for FRC contract
          if(t.isItFRContract()){
            if(t.transactionStatus==TransactionStatus.Cancelled.statusId){
              binding.routeDetails.nonExpHubIcon.setImageDrawable(ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.ic_black_hub))
              binding.routeDetails.nonExpHubIcon2.setImageDrawable(ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.ic_black_hub))
              binding.routeDetails.clNonExpTimeInterval1.background = ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.bg_all_round_corner_light_grey)
              binding.routeDetails.nonExpTimeInterval1.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.heading_black))
            }else{
              binding.routeDetails.nonExpHubIcon.setImageDrawable(ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.ic_hub_route))
              binding.routeDetails.nonExpHubIcon2.setImageDrawable(ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.ic_hub_route))
              binding.routeDetails.clNonExpTimeInterval1.background = ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.bg_all_round_corner_light_blue)
              binding.routeDetails.nonExpTimeInterval1.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.dark_blue))
            }
            binding.routeDetails.nonExpTvHubCity.text = t.clientName()
            binding.routeDetails.nonExpTvCity.text =  t.originCityName()
            binding.routeDetails.nonExpTvState.text =  ", "+t.originState
            binding.routeDetails.nonExpTvHubCity2.text = t.destinationCityName()
            binding.routeDetails.nonExpTvState2.text = t.destinationState
            binding.routeDetails.nonExpTimeInterval1.text  =
              t.tentativeTripCount?.let { Integer.toString(it) } +" Trips in "+ t.contractValidity+" weeks"
          }else if(t.isItIntraCityContract() && !t.isFlexible){
            if(t.transactionStatus== TransactionStatus.Cancelled.statusId){
              binding.routeDetails.intraCityHubIcon.setImageDrawable(ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.ic_black_hub))
              binding.routeDetails.intraCityReportingIcon.setImageDrawable(ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.ic_time_grey))
            }else{
              binding.routeDetails.intraCityHubIcon.setImageDrawable(ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.ic_hub_route))
              binding.routeDetails.intraCityReportingIcon.setImageDrawable(ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.ic_time))
            }
            binding.routeDetails.intraCityReportingTime.text = DateUtils.getFormattedTimeIn12Hrs(t.reportingTime?:"")
            binding.routeDetails.intraCityTvState.text = t.originState
            binding.routeDetails.intraCityTvCity.text = capitalize(t.originCity)
            binding.routeDetails.intraCityTvHubCity.text = t.origin
            binding.routeDetails.intraCityTvMapView.setOnClickListener{
              try {
                val gmmIntentUri = Uri.parse("geo:0,0?q=${t.latitude},${t.longitude}"+"(" + t.origin+ ")")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
              } catch (e: Exception) {
                Toast.makeText(this@ContractDetailsActivity, "Unable to open map", Toast.LENGTH_SHORT).show()
              }
            }
          }
          binding.contractRules.nonExpRules.visibility = t.isFRCContract()
          binding.contractRules.rules.visibility = t.isLHContract()
          binding.contractRules.intraCityRules.visibility = t.isIntraCityContract()
          binding.tripDetails.visibility = t.isLHContract()
          // For LH contract listing recycleview with halt centers
          if(_transaction.haltCenters!=null) {
            for (item in _transaction.haltCenters!!) {
             routesArray.add(item)
            }
            val contractsRouteDetailsAdapter  = ContractsRouteDetailsAdapter(routesArray,_transaction,this@ContractDetailsActivity)
            binding.routeDetails.rvContracts.apply {
              layoutManager = LinearLayoutManager(applicationContext)
              adapter = contractsRouteDetailsAdapter
            }
          }
          if(_transaction.secondaryReportingCenters!=null) {
            val originReportingCenters = SecondaryReportingCenters(_transaction.origin,_transaction.originCityName(),_transaction.originState,_transaction.longitude,_transaction.latitude)
            flexibleReportingCentersArray.add(originReportingCenters)
            for (item in _transaction.secondaryReportingCenters!!) {
              flexibleReportingCentersArray.add(item)
            }
            val contractIntracityFlexibleRCAdapter  = ContractIntracityFlexibleRCAdapter(flexibleReportingCentersArray,_transaction,this@ContractDetailsActivity)
            binding.routeDetails.rvIntracityFlexibleContracts.apply {
              layoutManager = LinearLayoutManager(applicationContext)
              adapter = contractIntracityFlexibleRCAdapter
            }
          }
         // binding.seperator.visibility = View.VISIBLE
        }
        binding.executePendingBindings()
      } else {
        binding.error = true
        binding.containerError.title = "Session Time Out"
        binding.containerError.subTitle =
          "Unfortunately, we couldn't fetch the data you are looking for. Kindly refresh."
        binding.containerError.actionLabel = "REFRESH"
      }
    }
  }

  /**
   * Transaction bid details UI updation observer
   */
  inner class TransactionBidObserver : Observer<BidDetailsUserBidState> {
    override fun onChanged(t: BidDetailsUserBidState) {
      binding.refreshing = false
      t?.let { state ->
        when (state) {
          // Transaction Cancel
          is BidDetailsContractCancelled ->{
            binding.cardInput.request = viewModel.transaction
            binding.cardInput.root.visibility = View.VISIBLE
            binding.cardInput.editBidCl.visibility = View.GONE
            binding.cardInput.rejectedBidCl.root.visibility = View.VISIBLE
            binding.cardInput.rejectedBidCl.title = "Demand cancelled"
            binding.cardInput.rejectedBidCl.subTitle = "The demand was cancelled by the client"
            binding.cardInput.rejectedBidCl.actionLabel = "Explore New Bids"
            binding.cardInput.rejectedBidCl.btnAction.setOnClickListener {
              // homeActivityIntent(HomeFragmentType.LoadsTruckFragment.title,this@BidDetailsActivity)
              startActivity(homeActivityIntent("load", this@ContractDetailsActivity))
              //action(NavigateHomeFragmentAction(HomeFragmentType.LoadsTruckFragment))
            }
            binding.mainCl.visibility = View.VISIBLE
            /*  binding.bottomLay.visibility = View.VISIBLE
              binding.confirmedText.text = getString(string.place_your_bid)
              binding.confirmedText.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.color_hint))
              binding.clBidYet.visibility = View.GONE
              binding.biddingIcon.setImageDrawable( ContextCompat.getDrawable(
                this@ContractDetailsActivity,
                R.drawable.ic_cancel_icon
              ))
              binding.bidClosingStatus.visibility = View.VISIBLE
              binding.bidClosingStatus.text = getString(string.bidding_cancelled)
              binding.confirmedText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_money_grey, 0, 0, 0)
              binding.buttonConfirm.background = ContextCompat.getDrawable(
                this@ContractDetailsActivity,
                R.drawable.bg_all_round_corner_light_grey
              )
              binding.buttonConfirm.isClickable = false
              binding.clBidStatus.visibility = View.VISIBLE
              binding.tvBidStatus.text = getString(string.cancelled)
              binding.tvBidTime.visibility = View.GONE
              binding.tvBidStatus.setTextColor(
                 ContextCompat.getColor(
                   this@ContractDetailsActivity,
                   R.color.heading_black
                 )
               )
              binding.statusImage.setImageDrawable(
                 ContextCompat.getDrawable(
                   this@ContractDetailsActivity,
                   R.drawable.ic_cancel_contract
                 )
               )
              binding.clBidStatus.background = ContextCompat.getDrawable(
                 this@ContractDetailsActivity,
                 R.drawable.bg_all_round_corner_light_grey
               )
  */
           }
          //Placed first bid
          is BidDetailsUserBidState_PlaceBidFirst -> {
            setBiddingInput(viewModel.transaction)
            binding.cardInput.root.visibility = View.VISIBLE
            enableKeyboard()
            /* binding.bottomLay.visibility = View.VISIBLE
             binding.confirmedText.text = getString(string.place_your_bid)
             binding.clBidYet.visibility = View.VISIBLE
             binding.buttonConfirm.setOnClickListener {
               bidDialog()
             }
             setBidStatusHeaderLayout(viewModel.transaction)*/
          }
          //Placed your first bid
          is BidDetailsUserBidState_PlaceBid -> {
            binding.cardInput.placeBidButton.text = "Place Bid"
            setBiddingInput(viewModel.transaction)
            binding.cardInput.root.visibility = View.VISIBLE
            enableKeyboard()
            /*  binding.bottomLay.visibility = View.VISIBLE
              binding.confirmedText.text = getString(string.place_your_bid)
              binding.clBidYet.visibility = View.VISIBLE
              binding.bidPmtFtl.visibility = View.GONE
              binding.buttonConfirm.setOnClickListener {
                bidDialog()
              }
              setBidStatusHeaderLayout(viewModel.transaction)*/
          }
          //Edit bid
          is BidDetailsUserBidState_EditBid -> {
            binding.cardInput.placeBidButton.text = "Revise Bid"
            viewModel.transaction.transactionBid = state.lowestAndUserBidPair.first
            setBiddingInput(viewModel.transaction)
            binding.cardInput.root.visibility = View.VISIBLE
            enableKeyboard()
            /*    binding.bottomLay.visibility = View.VISIBLE
                 binding.confirmedText.text = getString(string.revise_your_bid)
                 val data = viewModel.transaction
                 data.numBids = state.bidsCount
                 data.transactionBid = state.lowestAndUserBidPair.first
                 val userBid = state.lowestAndUserBidPair.first
                 val lowestTBid = state.lowestAndUserBidPair.second
                 data.lowestBid = lowestTBid?.bidAmount

                 // For intracity payment slabs if user has bids
                   showPayoutSlabs(data)
                 //for more than 1 bids and within live bidding
                 if(state.bidsCount>1 && data.isUnderOneHour()){
                   binding.clBidYet.visibility = View.GONE
                   binding.clExpressBidStatus.visibility = data.isLHIntraCityContract()
                   binding.clNonExpressBidStatus.visibility = data.isFRCContract()
                   //LH contract within live bidding
                   if(data.isItLHContract() || data.isItIntraCityContract()){
                     binding.bidPmtFtlStatus.visibility = View.VISIBLE
                     binding.bidReceived.text = state.bidsCount.toString()+" "+ getString(string.bid_received)
                     binding.bidAmount.text = "₹ "+StringUtils.formatAmount(userBid?.bidAmount!!)
                     if (lowestTBid?.bidAmount != null) {
                       // user bid same as Lowest bid
                       if (userBid?.bidAmount?.equals(lowestTBid?.bidAmount) == true) {
                         binding.bidStatus.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.bid_placed_green))
                         binding.bidStatus.background = ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.bg_all_rounded_won)
                         binding.bidStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_low_bid, 0, 0, 0)
                         binding.bidStatus.text = " Your bid is the lowest"
                       }else{
                         binding.bidStatus.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.destructive_red))
                         binding.bidStatus.background = ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.bg_all_rounded_lost_red)
                         binding.bidStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                         binding.bidStatus.text = getString(string.higher_than)+" "+data.contractLowestbidDifference()
                       }
                     }
                   }else{
                     // FRC contract within live bidding
                     binding.tripCommitted.text = userBid?.tentativeTripCount.toString()
                     binding.nonExpressBidReceived.text = state.bidsCount.toString()+ " Bids Received"
                     binding.nonExpressBidAmount.text = "₹ "+StringUtils.formatAmount(userBid?.bidAmount!!)
                     binding.nonExpressBidPmtFtlStatus.visibility = data.isFRCContract()
                     if(data.biddingType?.lowercase()=="pmt"){
                            binding.nonExpressBidPmtFtlStatus.text = getString(string.your_pmt_rate)
                             }else{
                            binding.nonExpressBidPmtFtlStatus.text = getString(string.your_ftl_rate)
                             }
                     // user bid same as Lowest bid
                     if (lowestTBid?.bidAmount != null) {
                       if (userBid?.bidAmount?.equals(lowestTBid?.bidAmount) == true ) {
                         binding.nonExpressBidStatus.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.bid_placed_green))
                         binding.nonExpressBidStatus.background = ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.bg_all_rounded_won)
                         binding.nonExpressBidStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_low_bid, 0, 0, 0)
                         binding.nonExpressBidStatus.text = " Your bid is the lowest"
                       }else{
                         binding.nonExpressBidStatus.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.destructive_red))
                         binding.nonExpressBidStatus.background = ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.bg_all_rounded_lost_red)
                         binding.nonExpressBidStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                         binding.nonExpressBidStatus.text = getString(string.higher_than)+" "+data.contractLowestbidDifference()
                       }
                     }
                   }
                 }else{
                         // if only 1 bid -> no target price or bid amount < Lowest bid
                         binding.clExpressBidStatus.visibility = View.GONE
                         binding.clNonExpressBidStatus.visibility =View.GONE
                         binding.clBidYet.visibility = View.VISIBLE
                         binding.bidUserStatusOrAmount.text = "₹ "+StringUtils.formatAmount(userBid?.bidAmount!!)
                         binding.bidPmtFtl.visibility = View.VISIBLE
                         if(data.isItLHContract()|| data.isItIntraCityContract()){
                           binding.bidPmtFtl.text =getString(string.your_bid)
                         }else{
                           if(data.biddingType?.lowercase()=="pmt"){
                             binding.bidPmtFtl.text = getString(string.your_pmt_rate)
                           }else{
                             binding.bidPmtFtl.text = getString(string.your_ftl_rate)
                           }
                         }



                 }
                 binding.buttonConfirm.setOnClickListener {
                   bidDialog(userBid)
                 }
                 setBidStatusHeaderLayout(data)*/
          }
          is BidDetailsUserBidState_LoadingBids -> {
            binding.cardInput.root.visibility = View.GONE
            binding.mainCl.visibility = View.GONE
            /*  binding.bottomLay.visibility = View.GONE
              uiUtils.showProgress()*/
          }
          //Result for closed bidding time
          is BidDetailsUserBidState_ContractResult -> {
            binding.cardInput.request = viewModel.transaction
            viewModel.transaction.transactionBid = state.lowestAndUserBidPair.first
            val data = viewModel.transaction
            data.transactionBid =  state.lowestAndUserBidPair.first
            data.numBids = state.bidsCount
            val userBid = state.lowestAndUserBidPair.first
            val lowestTBid = state.lowestAndUserBidPair.second
            //data.lowestBid =lowestTBid?.bidAmount
            showPayoutSlabs(data)
            if(userBid?.status()==Open){
              binding.cardInput.editBidCl.visibility = View.GONE
              binding.cardInput.root.visibility = View.VISIBLE
              binding.cardInput.awaitingBidCl.root.visibility = View.VISIBLE
              binding.cardInput.awaitingBidCl.title = "Awaiting Result"
              binding.cardInput.awaitingBidCl.subTitle = "We’ll notify you once the results are out"
              binding.cardInput.awaitingBidCl.actionLabel = "Explore New Bids"
              binding.cardInput.awaitingBidCl.btnAction.setOnClickListener {
                startActivity(homeActivityIntent("load", this@ContractDetailsActivity))

                // fragmentAction(NavigateHomeFragmentAction(HomeFragmentType.PlacementsFragment))
              }
            }else if(userBid?.status()==Accepted){
              binding.cardInput.editBidCl.visibility = View.GONE
              binding.cardInput.root.visibility = View.VISIBLE
              binding.cardInput.confirmedBidCl.root.visibility = View.VISIBLE
              binding.cardInput.confirmedBidCl.title = "Bid Confirmed for ₹"+ binding.transaction?.transactionBid?.bidAmount?.let {
                StringUtils.formatDecimalAmount(
                  it
                )
              }
              binding.cardInput.confirmedBidCl.subTitle = "Provide the driver and vehicle details"
              binding.cardInput.confirmedBidCl.actionLabel = "Go To Placement Tab"
              binding.cardInput.confirmedBidCl.btnAction.setOnClickListener {
                startActivity(homeActivityIntent("placement", this@ContractDetailsActivity))

                // fragmentAction(NavigateHomeFragmentAction(HomeFragmentType.PlacementsFragment))
              }
            }else if (userBid?.status()==Cancelled){
              binding.cardInput.root.visibility = View.VISIBLE
              binding.cardInput.editBidCl.visibility = View.GONE
              binding.cardInput.rejectedBidCl.root.visibility = View.VISIBLE
              binding.cardInput.rejectedBidCl.title = "Demand cancelled"
              binding.cardInput.rejectedBidCl.subTitle = "The demand was cancelled by the client"
              binding.cardInput.rejectedBidCl.actionLabel = "Explore New Bids"
              binding.cardInput.rejectedBidCl.btnAction.setOnClickListener {
                // homeActivityIntent(HomeFragmentType.LoadsTruckFragment.title,this@BidDetailsActivity)
                startActivity(homeActivityIntent("load", this@ContractDetailsActivity))
                //action(NavigateHomeFragmentAction(HomeFragmentType.LoadsTruckFragment))
              }
            }else if (userBid?.status()==Rejected){
              binding.cardInput.root.visibility = View.VISIBLE
              binding.cardInput.editBidCl.visibility = View.GONE
              binding.cardInput.rejectedBidCl.root.visibility = View.VISIBLE
              binding.cardInput.rejectedBidCl.title = "Bid not selected"
              try{
                binding.cardInput.rejectedBidCl.subTitle = "Winning bid price is ₹${DecimalFormat("#########").format(state.lowestAndUserBidPair.second?.second?.bidAmount?.toInt())}"
              }catch (e:Exception){
              }
              binding.cardInput.rejectedBidCl.actionLabel = "Explore New Bids"
              binding.cardInput.rejectedBidCl.btnAction.setOnClickListener {
                //   homeActivityIntent(HomeFragmentType.LoadsTruckFragment.title,this@BidDetailsActivity)
                startActivity(homeActivityIntent("load", this@ContractDetailsActivity))

                // fragmentAction(NavigateHomeFragmentAction(HomeFragmentType.PlacementsFragment))
              }
              }else{

              }
            binding.mainCl.visibility = View.VISIBLE
            binding.cardInput.root.visibility = View.VISIBLE
          //  setBiddingInput(viewModel.transaction)
            /*val data = viewModel.transaction
            data.transactionBid =  state.lowestAndUserBidPair.first
            data.numBids = state.bidsCount
            val userBid = state.lowestAndUserBidPair.first
            val lowestTBid = state.lowestAndUserBidPair.second
             data.lowestBid =lowestTBid?.bidAmount
             showPayoutSlabs(data)
            binding.bottomLay.visibility = View.GONE
            binding.viewMarginResult.visibility = View.VISIBLE

            binding.viewMarginRoute.visibility = View.VISIBLE
            binding.viewMarginRule.visibility = View.VISIBLE

            binding.contractResult.bidAmountLayout.visibility = View.VISIBLE
            binding.contractResult.seperator.visibility = data.isFRCContract()
            binding.contractResult.yourTripCommitment.visibility =data.isFRCContract()
            binding.contractResult.yourTripCommitmentValue.visibility =data.isFRCContract()
            binding.contractRulesWithResult.rules.visibility = data.isLHContract()
            binding.contractRulesWithResult.intraCityRules.visibility = data.isIntraCityContract()
            binding.contractRulesWithResult.nonExpRules.visibility = data.isFRCContract()
            binding.contractRules.rules.visibility = View.GONE
            binding.contractRules.nonExpRules.visibility = View.GONE
            binding.contractRules.intraCityRules.visibility = View.GONE
            if(data.isItFRContract()){
              binding.contractResult.yourTripCommitmentValue.text = data.transactionBid?.tentativeTripCount?.toString()
            }
            binding.viewMarginWithoutResult.visibility = View.GONE
              if(userBid?.bidAmount==null){
              binding.contractResult.haveNoBid.visibility = View.VISIBLE
                binding.contractResult.contractBidResultLayout.visibility = View.GONE
            }else{
              binding.contractResult.haveNoBid.visibility = View.GONE
                binding.contractResult.contractBidResultLayout.visibility = View.VISIBLE
            }
            if(userBid?.status()==Open){
              binding.contractResult.resultDescribe.visibility = View.VISIBLE
              binding.contractResult.yourBidAmountValue.text = "₹"+StringUtils.formatAmount(userBid?.bidAmount!!)
            }else if(userBid?.status()==Accepted){
              binding.contractResult.resultDescribe.visibility = View.VISIBLE
              binding.contractResult.resultDescribe.text = getString(string.won_contracts)
              binding.contractResult.yourBidAmountValue.text = "₹"+StringUtils.formatAmount(userBid?.bidAmount!!)
            }else{
              if(userBid?.bidAmount!=null) {
                binding.contractResult.resultDescribe.visibility = View.VISIBLE
                binding.contractResult.yourBidAmountValue.text =
                  "₹" + StringUtils.formatAmount(userBid?.bidAmount!!)
                if (lowestTBid?.bidAmount != null) {
                  if (userBid?.bidAmount?.equals(lowestTBid?.bidAmount) == true) {
                    binding.contractResult.resultDescribe.text =
                      getString(string.not_meeting_expectation)
                  } else {
                    if (data.userBidLessThanFivePercentage()) {
                      binding.contractResult.resultDescribe.text =
                        getString(string.less_than_5_percentage)
                    } else {
                      binding.contractResult.resultDescribe.text =
                        getString(string.more_than_5_percentage)

                    }

                  }
                } else {
                  binding.contractResult.resultDescribe.visibility = View.GONE
                }
              }
            }
            if(userBid?.status()==Open) {
              binding.clBidStatus.visibility = View.VISIBLE
              binding.tvBidTime.text = getString(string.awaiting_results)
              binding.tvBidStatus.text=getString(string.bidding_closed)
              binding.tvBidTime.visibility = View.VISIBLE
              binding.tvBidStatus.setTextColor(
                ContextCompat.getColor(
                  this@ContractDetailsActivity,
                  R.color.dark_blue
                )
              )
              binding.statusImage.setImageDrawable(
                ContextCompat.getDrawable(
                  this@ContractDetailsActivity,
                  R.drawable.ic_awaiting_bid
                )
              )
              binding.clBidStatus.background = ContextCompat.getDrawable(
                this@ContractDetailsActivity,
                R.drawable.bg_all_rounded_under_review
              )
            }else{

              binding.clBidStatus.visibility = View.VISIBLE
              binding.tvBidStatus.text = getString(string.result_declared)
              binding.tvBidTime.visibility = View.GONE
              binding.tvBidStatus.setTextColor(
                ContextCompat.getColor(
                  this@ContractDetailsActivity,
                  R.color.bid_placed_green
                )
              )
              binding.statusImage.setImageDrawable(
                ContextCompat.getDrawable(
                  this@ContractDetailsActivity,
                  R.drawable.ic_bid_result
                )
              )
              binding.clBidStatus.background = ContextCompat.getDrawable(
                this@ContractDetailsActivity,
                R.drawable.bg_all_rounded_won
              )
            }*/
          }

          else -> null
        }
        }
      }
    }

  private fun showPayoutSlabs(data:HomeBidsRequestItemData) {
    if(data.paymentSlabs!=null && data.isItIntraCityContract()&& data.transactionStatus!=TransactionStatus.Cancelled.statusId) {
      val keys = data.paymentSlabs!!.keySet()
      binding.transaction = data
      paymentSlabsArray = ArrayList()
      paymentSlabsArray.add(PaymentSlabs("Kms","Monthly Payout"))
      for (key in keys) {
        val value = data.paymentSlabs!!.get(key).asString
        paymentSlabsArray.add(PaymentSlabs(key,"₹ "+value))
      }
        binding.paymentSlabsMoreSlab.visibility = if(data.isPaymentSlabsVisible()&&keys.size>3) View.VISIBLE else View.GONE

      val contractPaymentSlabsAdapter  = ContractPaymentSlabsAdapter(paymentSlabsArray,data,this@ContractDetailsActivity)
      binding.rvPaymentSlabs.apply {
        layoutManager = LinearLayoutManager(applicationContext)
        adapter = contractPaymentSlabsAdapter
      }
      var isExpanded = false
      binding.paymentSlabsMoreSlab.text = getString(string.see_full_slabs_list)
      binding.paymentSlabsMoreSlab.setOnClickListener {
        isExpanded = !isExpanded
        contractPaymentSlabsAdapter.expand(isExpanded)
        if(isExpanded){
          binding.paymentSlabsMoreSlab.text = getString(string.hide_slabs)
        }else{
          binding.paymentSlabsMoreSlab.text = getString(string.see_full_slabs_list)
        }

      }
    }
  }

  // set header status and bottom button
/*   fun setBidStatusHeaderLayout(data:HomeBidsRequestItemData) {
     if (data.isContractBiddingOpen()) {
     if (data.isUnderOneHour()) {
       binding.clBidStatus.visibility = View.VISIBLE
       binding.tvBidStatus.text = getString(string.live_bidding)
       binding.tvBidStatus.setTextColor(
         ContextCompat.getColor(
           this@ContractDetailsActivity,
           R.color.destructive_red
         )
       )
       GlideApp.with(this@ContractDetailsActivity)
         .load(R.raw.livebidding)
         .apply(RequestOptions().override(40, 40))
         .into(binding.statusImage)

       binding.clBidStatus.background = ContextCompat.getDrawable(
         this@ContractDetailsActivity,
         R.drawable.bg_all_rounded_lost_red
       )
     } else {
       binding.clBidStatus.visibility = View.VISIBLE
       binding.tvBidStatus.text = getString(string.collecting_bids)
       binding.tvBidTime.text = data.bidEndDate()

       binding.tvBidTime.visibility = View.VISIBLE
       binding.tvBidStatus.setTextColor(
         ContextCompat.getColor(
           this@ContractDetailsActivity,
           R.color.pending_status
         )
       )
       binding.statusImage.setImageDrawable(
         ContextCompat.getDrawable(
           this@ContractDetailsActivity,
           R.drawable.ic_money_pending
         )
       )
       binding.clBidStatus.background = ContextCompat.getDrawable(
         this@ContractDetailsActivity,
         R.drawable.bg_all_rounded_pending
       )
     }
       setTimer(data.contractBiddingEndTime!!)

   }else{
       binding.clBidStatus.visibility = View.VISIBLE
       binding.tvBidTime.text = getString(string.awaiting_results)
       binding.tvBidStatus.text=getString(string.bidding_closed)
       binding.tvBidTime.visibility = View.VISIBLE
       binding.tvBidStatus.setTextColor(
         ContextCompat.getColor(
           this@ContractDetailsActivity,
           R.color.dark_blue
         )
       )
       binding.statusImage.setImageDrawable(
         ContextCompat.getDrawable(
           this@ContractDetailsActivity,
           R.drawable.ic_awaiting_bid
         )
       )
       binding.clBidStatus.background = ContextCompat.getDrawable(
         this@ContractDetailsActivity,
         R.drawable.bg_all_rounded_under_review
       )
            binding.bottomLay.visibility = View.GONE
            binding.viewMarginResult.visibility = View.VISIBLE
            binding.contractResult.haveNoBid.visibility = View.VISIBLE
            binding.viewMarginRoute.visibility = View.VISIBLE
            binding.viewMarginRule.visibility = View.VISIBLE
            binding.contractResult.contractBidResultLayout.visibility = View.GONE
            binding.contractResult.bidAmountLayout.visibility = View.GONE
            binding.contractResult.seperator.visibility = View.GONE
            binding.contractResult.yourTripCommitment.visibility = View.GONE
            binding.contractResult.yourTripCommitmentValue.visibility =View.GONE
            binding.contractRulesWithResult.rules.visibility = data.isLHContract()
            binding.contractRulesWithResult.nonExpRules.visibility = data.isFRCContract()
            binding.contractRulesWithResult.intraCityRules.visibility = data.isIntraCityContract()
            binding.contractRules.rules.visibility = View.GONE
            binding.contractRules.nonExpRules.visibility = View.GONE
            binding.contractRules.intraCityRules.visibility = View.GONE
            if(data.isItFRContract()){
              binding.contractResult.yourTripCommitmentValue.text = data.transactionBid?.tentativeTripCount?.toString()
            }
            binding.viewMarginWithoutResult.visibility = View.GONE
    }
   }*/
  override fun getViewModelClass()= ContractDetailsViewModel::class.java

  override fun layoutId(): Int= R.layout.activity_contract_details
  override fun requireConnection(): Boolean = true

  private fun refreshData() {
    binding.error = false
    viewModel.fetchTransactionDetails()
    binding.executePendingBindings()
    routesArray.clear()
    flexibleReportingCentersArray.clear()
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
        startActivity(homeActivityIntent("load", this@ContractDetailsActivity))
        finish()
      }
    }, 5000)
  }
  private fun showSuccessPlaceReviseDialog(bidInfo:Triple<Pair<String,String>,String?,Pair<Boolean,Boolean>>){
       REFRESH_ON_BACK = true
      val dialog = Dialog(this)
      val bindingDialog = DialogContractsBidSuccessBinding.inflate(this.layoutInflater)
      bindingDialog.transaction = viewModel.transaction
      bindingDialog.buttonCancel.setOnClickListener {
        dialog.cancel()
        uiUtils.hideProgress()
         refreshData()
      }
       dialog.setCancelable(false)
      if(bidInfo.third.second){
        bindingDialog.title.text = getString(string.bid_revise_successfully)
      }else{
        bindingDialog.title.text = getString(string.bid_placed_sucessfully)
      }
       if(viewModel.transaction.isItLHContract()){
         bindingDialog.yourBidAmountValue.text = "₹ "+bidInfo.first.first
       }else if(viewModel.transaction.isItFRContract()){
         bindingDialog.yourTripValue.text = bidInfo.second
         if(bidInfo.third.first){
         bindingDialog.yourBidAmountValue.text = "₹ "+bidInfo.first.second+ "\n (PMT)"
         }
         else{
           bindingDialog.yourBidAmountValue.text= "₹ "+bidInfo.first.first+ "\n (FTL)"
         }
       }else if(viewModel.transaction.isItIntraCityContract()){
         bindingDialog.yourVehicleValue.text = bidInfo.second
         bindingDialog.yourBidAmountValue.text= "₹ "+bidInfo.first.first
       }

      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
      dialog.setContentView(bindingDialog.root)
      dialog.show()
      dialog.window!!.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
      )
  }

  override fun bidPlacedSuccess(success: Boolean) {
    if(success)
      startActivity(homeActivityIntent("load", this@ContractDetailsActivity))

  }

}

/* intent keys */
private const val TransactionIdIntentKey = "transaction_id"

/* intent keys */
private const val RequestTypeIntentKey = "request_type"
/**
 * Bid details intent
 */
fun contractDetailsIntent(
  transactionId: String,
  context: Context,
  source: String?= VALUE_APP_FLOW,
) = Intent(context, ContractDetailsActivity::class.java).apply {
  putExtra(TransactionIdIntentKey, transactionId)
  putExtra(PROPERTY_SOURCE,source)

}