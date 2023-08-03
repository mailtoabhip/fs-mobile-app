package com.delhivery.axle.ui.contractDetails

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.DialogBidCreateEditBinding
import com.delhivery.axle.databinding.DialogContractsCreateEditBidBinding
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialogInterface
import com.delhivery.axle.utils.AnalyticsUtil
import com.delhivery.axle.utils.EVENT_EDIT_BID
import com.delhivery.axle.utils.EVENT_HOME_CONTRACT_TAB_CLICK
import com.delhivery.axle.utils.EVENT_PLACE_BID
import com.delhivery.axle.utils.EVENT_REVISE_BID_INTENT
import com.delhivery.axle.utils.EVENT_REVISE_CONTRACT_BID
import com.delhivery.axle.utils.EVENT_SUBMIT_CONTRACT_BID
import com.delhivery.axle.utils.PROPERTY_BID_AMOUNT_DIFF
import com.delhivery.axle.utils.PROPERTY_CONTRACT_TYPE
import com.delhivery.axle.utils.PROPERTY_DEMAND_TYPE
import com.delhivery.axle.utils.PROPERTY_ORDER_ID
import com.delhivery.axle.utils.PROPERTY_OVERALL_PERFORMANCE
import com.delhivery.axle.utils.PROPERTY_PAGE_NAME
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_SOURCE
import com.delhivery.axle.utils.PROPERTY_STATUS
import com.delhivery.axle.utils.PROPERTY_TIME_LAPSE
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_ID
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.errorVibrate
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.setup
import com.delhivery.axle.utils.prefs.UserPrefs
import okhttp3.internal.Util
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.math.abs

class ContractDetailsCreateEditDialog @Inject constructor(
  context: Context,
  private val transaction: HomeBidsRequestItemData,
  private val transactionBid: TransactionBid? = null, /* transaction bid null for create new bid */
  private val dialogInterface: ContractDetailsCreateEditDialogInterface,
  private val position: Int = 0,
  private val analyticsUtil: AnalyticsUtil,
  private var userPrefs: UserPrefs,
  private var source: String
) : AlertDialog(context) {

  /* dialog binding */
  private lateinit var binding: DialogContractsCreateEditBidBinding
  private var amount = 0
  private var pmtRate = 0
  private var isChecked = false
  private var isValidBidAmount = false
  private var isValidTripCommit = false
  private var isValidVehicleNumber = false
  private var isValidPlacementDays = false


  private var expectedArrivalTimePickup = ""
  private var expectedArrivalTimePickupRemark = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window?.clearFlags(
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
    )

   // setCancelable(false)

    /* dialog binding */
    binding = DialogContractsCreateEditBidBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val items= context.resources.getStringArray(R.array.placementDaysItems)

    val spinnerAdapter= object : ArrayAdapter<String>(context,android.R.layout.simple_spinner_item, items) {

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
    binding.spinnerPlacementDays.adapter = spinnerAdapter
    /* set binding params */
    binding.apply {
      request = transaction
      if(transaction.isItIntraCityContract()&& transaction.isFlexible){
        val moreCenters =transaction.getMoreCentersCount()
        binding.intraCityRouteInfo.text = if(moreCenters>0){HtmlCompat.fromHtml(context.getString(R.string.msg_more,transaction.reportingCenters(),moreCenters), HtmlCompat.FROM_HTML_MODE_LEGACY)}else transaction.reportingCenters()
      }else if(transaction.isItIntraCityContract()){
        binding.intraCityRouteInfo.text = transaction.tripContractRoute()
      }
      route = transaction.tripContractRoute()
        transactionBid?.bidAmount?.let {
          isValidBidAmount= true
          binding.title.text = "Revise Your Bid"
          binding.bidAmountLbl.text = "Revised Bid Amount"
          binding.editBidAmount?.setText(DecimalFormat("#########").format(it))
          if(transaction.isPMTIndent()){
            pmtRate = it.toInt()
            amount = (it*transaction.requestedCapacityMg).toInt()
          }else{
            amount = it.toInt()
          }
        }
        transactionBid?.tentativeTripCount?.let {
          isValidTripCommit = true
          binding.editTripCommitted?.setText(DecimalFormat("#########").format(it))
        }
      transactionBid?.vehicleNumber?.let {
        isValidVehicleNumber = true
        binding.editVehicleNumber?.setText(it)
      }
      transactionBid?.placementDays?.let {
        isValidPlacementDays = true
        if(it.equals("1-2 Days")) {
          binding.spinnerPlacementDays.setSelection(1)
        }else if(it.equals("3-5 Days")) {
          binding.spinnerPlacementDays.setSelection(2)
        }else if(it.equals("6-8 Days")) {
          binding.spinnerPlacementDays.setSelection(3)
        }
      }
      enableSubmit()
    }




    binding.editBidAmount?.addTextChangedListener(object : TextWatcher {
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
                  if(transaction.targetPrice!=null&&(amount>(transaction.targetPrice+transaction.targetPrice*upperPercentage/100)|| amount<(transaction.targetPrice-transaction.targetPrice*lowerPercentage/100))){
                    showInvalidBidAmountError()
                  }else{
                    removeInvalidBidAmountError()
                  }
              }
              enableSubmit()
            }else{
              amount = input
              if(amount<250){
                showInvalidBidAmountError()
              }else{
                  if (transaction.targetPrice!=null&&(amount > (transaction.targetPrice + transaction.targetPrice * upperPercentage / 100) || amount <(transaction.targetPrice - transaction.targetPrice * lowerPercentage / 100))) {
                    showInvalidBidAmountError()
                  } else {
                    if (transactionBid != null) {
                      if(transaction.isItIntraCityContract()){
                        if (abs(amount-transactionBid.bidAmount.toInt())>=100 && abs(amount-transactionBid.bidAmount.toInt())%100==0) {
                          removeInvalidBidAmountError()
                        }else{
                          showInvalidBidAmountError("100")
                        }
                      }else{
                        if(amount<12000){
                          if (abs(amount-transactionBid.bidAmount.toInt())>=50 && abs(amount-transactionBid.bidAmount.toInt())%50==0) {
                            removeInvalidBidAmountError()
                          }else{
                            showInvalidBidAmountError("50")
                          }
                        }else{
                          if (abs(amount-transactionBid.bidAmount.toInt())>=250 && abs(amount-transactionBid.bidAmount.toInt())%250==0) {
                            removeInvalidBidAmountError()
                          }else{
                            showInvalidBidAmountError("250")
                          }
                        }
                      }
                    }else{
                      removeInvalidBidAmountError()
                    }
                  }

                enableSubmit()
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
          enableSubmit()
        }
      }
    })

    binding.editTripCommitted?.addTextChangedListener(object : TextWatcher {
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
              binding.tripCountError.visibility = View.VISIBLE
              binding.tripCountError.text ="Invalid trip count"
              isValidTripCommit = false
            }else if (input > 0 && input > transaction.tentativeTripCount!!) {
              binding.tripCountError.text =context.getString(R.string.exceeds_tentative_trip_count)
              binding.tripCountError.visibility = View.VISIBLE
              isValidTripCommit = false

            } else {
              isValidTripCommit = true
              binding.tripCountError.visibility = View.GONE

            }
            enableSubmit()
          }else{
            isValidTripCommit = true
            enableSubmit()
          }
        }else{
          if(transaction.isItFRContract()){
            isValidTripCommit = false
            enableSubmit()
          }
        }
      }
    })


    binding.editVehicleNumber?.addTextChangedListener(object : TextWatcher {
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
              binding.vehicleNumberError.visibility = View.GONE
            } else {
              binding.vehicleNumberError.visibility = View.VISIBLE
              binding.vehicleNumberError.text ="Invalid vehicle Number"
              isValidVehicleNumber = false

            }
            enableSubmit()
          }else{
            isValidVehicleNumber = true
            enableSubmit()
          }
        }else{
          if(transaction.isItIntraCityContract()){
            isValidVehicleNumber = false
            enableSubmit()
          }
        }
      }
    })




    binding.spinnerPlacementDays.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
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
          enableSubmit()
        }else{
          isValidPlacementDays = true
          enableSubmit()
        }
      }

    }

    binding.buttonSubmit.setOnClickListener {
      binding.editBidAmount.clearFocus()
        submit()

    }

    binding.buttonCancel.setOnClickListener { dismiss() }
  }

  private fun  enableSubmit(){
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
      if (transactionBid != null) {
        // checking if value has changed
        if(checkValueChangesForLHFTL()||checkValueChangeForFRC()||checkValueChangesForIntraCity()) {
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
    if(amount!=null){
      binding.bidError.text = context.getString(string.bid_revise_error)+amount
    }else{
      binding.bidError.text =  "Invalid Bid Amount, Out of range"
    }
    binding.bidError.visibility = View.VISIBLE
  }

  private fun removeInvalidBidAmountError(){
    isValidBidAmount = true
    binding.bidError.visibility = View.GONE
  }
  private fun checkValueChangeForFRC(): Boolean{
    return if (transactionBid != null) (transaction.isItFRContract()&&Integer.parseInt(binding.editTripCommitted.text.toString()) == transactionBid.tentativeTripCount
              && if(transaction.isPMTIndent())amount == (transactionBid!!.bidAmount*transaction.requestedCapacityMg).toInt() else amount==transactionBid.bidAmount.toInt())
          else false
  }

  private fun checkValueChangesForLHFTL(): Boolean{
    return if (transactionBid != null)(transaction.isItLHContract() && if(transaction.isPMTIndent())amount == (transactionBid!!.bidAmount*transaction.requestedCapacityMg).toInt() else amount==transactionBid.bidAmount.toInt())
          else false
  }

  private fun checkValueChangesForIntraCity(): Boolean{
    return if (transactionBid != null)(transaction.isItIntraCityContract() &&if(transaction.isPMTIndent())amount == (transactionBid!!.bidAmount*transaction.requestedCapacityMg).toInt() else amount==transactionBid.bidAmount.toInt() && binding.editVehicleNumber.text.toString() == transactionBid.vehicleNumber && binding.spinnerPlacementDays.selectedItem.toString() == transactionBid.placementDays)
          else false
  }

  private fun disableSubmitButton(){
    binding.buttonSubmit.background =
      ContextCompat.getDrawable(context, R.drawable.bg_all_round_corner_grey_boundary)
    binding.buttonSubmit.isEnabled = false
  }
  private fun enableSubmitButton(){
    binding.buttonSubmit.isEnabled = true
    binding.buttonSubmit.background =
      ContextCompat.getDrawable(context, R.drawable.bg_all_rounded_blue_corner)
  }
  private fun submit() {
    try {
        if (transactionBid == null) {
          analyticsUtil.moEngageTrackEvent(
            EVENT_SUBMIT_CONTRACT_BID,mutableListOf(PROPERTY_USER_ID,
              PROPERTY_PHONE_NO,
              PROPERTY_CONTRACT_TYPE, PROPERTY_STATUS,PROPERTY_ORDER_ID, PROPERTY_SOURCE),
            mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:"",transaction.contractType?:"",transaction.contractEventStatusText()?:"", transaction.key(),source))
          dialogInterface.createBid(
            transaction.isPMTIndent(), transaction.key(), amount, pmtRate,
            transaction.biddingType
              ?: "FTL", position,if(binding.editTripCommitted.text.isNullOrEmpty())null else Integer.parseInt(binding.editTripCommitted.text.toString()),if(binding.editVehicleNumber.text.isNullOrEmpty())null else binding.editVehicleNumber.text.toString().uppercase(),if(transaction.isItIntraCityContract())binding.spinnerPlacementDays.selectedItem.toString() else null
          )
        } else {
          analyticsUtil.moEngageTrackEvent(
            EVENT_REVISE_CONTRACT_BID,mutableListOf(PROPERTY_USER_ID,
              PROPERTY_PHONE_NO,
              PROPERTY_CONTRACT_TYPE, PROPERTY_STATUS, PROPERTY_ORDER_ID, PROPERTY_BID_AMOUNT_DIFF, PROPERTY_SOURCE),
            mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:"",transaction.contractType?:"",transaction.contractEventStatusText()?:"", transaction.key(), (amount-transactionBid?.bidAmount!!).toString(),source))

          dialogInterface.editBid(
            transaction.isPMTIndent(), transaction.key(), transactionBid.key(),
            amount, pmtRate, transaction.biddingType ?: "FTL", position,if(binding.editTripCommitted.text.isNullOrEmpty())null else Integer.parseInt(binding.editTripCommitted.text.toString()),if(binding.editVehicleNumber.text.isNullOrEmpty())null else binding.editVehicleNumber.text.toString().uppercase(),if(transaction.isItIntraCityContract())binding.spinnerPlacementDays.selectedItem.toString() else null
          )
        }
        dismiss()

    } catch (e: IllegalArgumentException) {

    }
  }
}

interface ContractDetailsCreateEditDialogInterface {

  /**
   * Create bid
   */
  fun createBid(
    isPMT: Boolean,
    transactionId: String,
    bidAmount: Int,
    pmtRate: Int,
    commercialType: String,
    position: Int = -1,
    tentativeTripCount:Int?,
    vehicleNumber:String?,
    placementDays:String?
  )

  /**
   * Edit bid
   */
  fun editBid(
    isPMT: Boolean,
    transactionId: String,
    bidId: String,
    bidAmount: Int,
    pmtRate: Int,
    commercialType: String,
    position: Int = -1,
    tentativeTripCount:Int?,
    vehicleNumber:String?,
    placementDays:String?
  )

}