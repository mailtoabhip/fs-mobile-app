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
import com.delhivery.axle.R
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
import com.delhivery.axle.utils.PROPERTY_STATUS
import com.delhivery.axle.utils.PROPERTY_TIME_LAPSE
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_ID
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.errorVibrate
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.android.synthetic.main.activity_bid_details.tvRemark
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
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
  private var fromPage: String
) : AlertDialog(context) {

  /* dialog binding */
  private lateinit var binding: DialogContractsCreateEditBidBinding
  private var amount = 0
  private var pmtRate = 0
  private var isChecked = false
  private var isValidBidAmount = false
  private var isValidTripCommit = false

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

    /* set binding params */
    binding.apply {
      request = transaction
      route = transaction.tripContractRoute()
        transactionBid?.bidAmount?.let {
          binding.title.text = "Revise Your Bid"
          binding.editBidAmount?.setText(DecimalFormat("#########").format(it))
        }
        transactionBid?.tentativeTripCount?.let {
          isValidTripCommit = true
          binding.editTripCommitted?.setText(DecimalFormat("#########").format(it))
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
              lowerPercentage  = 20
              upperPercentage  = 20
            }else{
              lowerPercentage  = 20
              upperPercentage  = 10
            }
            if (transaction.isPMTIndent()) {
              pmtRate = input
              amount = (input * transaction.requestedCapacityMg).toInt()
              if(amount<250){
                isValidBidAmount = false
                binding.bidError.text = "Invalid Bid Amount, Out of range"
                binding.bidError.visibility = View.VISIBLE
              }else{
                  if(transaction.targetPrice!=null&&(amount>(transaction.targetPrice+transaction.targetPrice*upperPercentage/100)|| amount<(transaction.targetPrice-transaction.targetPrice*lowerPercentage/100))){
                    isValidBidAmount = false
                    binding.bidError.text = "Invalid Bid Amount, Out of range"
                    binding.bidError.visibility = View.VISIBLE
                  }else{
                    isValidBidAmount = true
                    binding.bidError.visibility = View.GONE
                  }
              }
              enableSubmit()
            }else{
              amount = input
              if(amount<250){
                isValidBidAmount = false
                binding.bidError.text =  "Invalid Bid Amount, Out of range"
                binding.bidError.visibility = View.VISIBLE
              }else{
                  if (transaction.targetPrice!=null&&(amount >= (transaction.targetPrice + transaction.targetPrice * upperPercentage / 100) || amount <=(transaction.targetPrice - transaction.targetPrice * lowerPercentage / 100))) {
                    isValidBidAmount = false
                    binding.bidError.text = "Invalid Bid Amount, Out of range"
                    binding.bidError.visibility = View.VISIBLE
                  } else {
                    if (transactionBid != null) {
                      if(amount<12000){
                        if (abs(amount-transactionBid.bidAmount.toInt())>=50 && abs(amount-transactionBid.bidAmount.toInt())%50==0) {
                          isValidBidAmount = true
                          binding.bidError.visibility = View.GONE
                        }else{
                          isValidBidAmount = false
                          binding.bidError.text = "Bid difference should be multiple of 50"
                          binding.bidError.visibility = View.VISIBLE
                        }
                      }else{
                        if (abs(amount-transactionBid.bidAmount.toInt())>=250 && abs(amount-transactionBid.bidAmount.toInt())%250==0) {
                          isValidBidAmount = true
                          binding.bidError.visibility = View.GONE
                        }else{
                          isValidBidAmount = false
                          binding.bidError.text = "Bid difference should be multiple of 250"
                          binding.bidError.visibility = View.VISIBLE
                        }
                      }

                    }else{
                      isValidBidAmount = true
                      binding.bidError.visibility = View.GONE
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
          if(!transaction.isItLHContract()) {
            if (input > 0 && input > transaction.tentativeTripCount!!) {
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
        }
      }
    })



    binding.buttonSubmit.setOnClickListener {
      binding.editBidAmount.clearFocus()
        submit()

    }

    binding.buttonCancel.setOnClickListener { dismiss() }
  }

  private fun  enableSubmit(){
     if(transaction.isItLHContract()){
       isValidTripCommit = true
     }
    if(isValidBidAmount&&isValidTripCommit){
      binding.buttonSubmit.isEnabled = true
        binding.buttonSubmit.background = ContextCompat.getDrawable(context,R.drawable.bg_all_rounded_blue_corner)
    }else{
      binding.buttonSubmit.background = ContextCompat.getDrawable(context,R.drawable.bg_all_round_corner_grey_boundary)
      binding.buttonSubmit.isEnabled = false
    }
  }

  private fun submit() {
    try {
        if (transactionBid == null) {
          analyticsUtil.moEngageTrackEvent(
            EVENT_SUBMIT_CONTRACT_BID,mutableListOf(PROPERTY_USER_ID,
              PROPERTY_PHONE_NO,
              PROPERTY_CONTRACT_TYPE, PROPERTY_STATUS,PROPERTY_ORDER_ID),
            mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:"",transaction.contractType?:"",transaction.contractEventStatusText()?:"", transaction.key()))
          dialogInterface.createBid(
            transaction.isPMTIndent(), transaction.key(), amount, pmtRate,
            transaction.biddingType
              ?: "FTL", position,if(binding.editTripCommitted.text.isNullOrEmpty())null else Integer.parseInt(binding.editTripCommitted.text.toString())
          )
        } else {
          analyticsUtil.moEngageTrackEvent(
            EVENT_REVISE_CONTRACT_BID,mutableListOf(PROPERTY_USER_ID,
              PROPERTY_PHONE_NO,
              PROPERTY_CONTRACT_TYPE, PROPERTY_STATUS, PROPERTY_ORDER_ID, PROPERTY_BID_AMOUNT_DIFF),
            mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:"",transaction.contractType?:"",transaction.transactionStatus?:"", transaction.key(), (amount-transactionBid?.bidAmount!!).toString()))

          dialogInterface.editBid(
            transaction.isPMTIndent(), transaction.key(), transactionBid.key(),
            amount, pmtRate, transaction.biddingType ?: "FTL", position,if(binding.editTripCommitted.text.isNullOrEmpty())null else Integer.parseInt(binding.editTripCommitted.text.toString())
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
    tentativeTripCount:Int?
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
    tentativeTripCount:Int?
  )

}