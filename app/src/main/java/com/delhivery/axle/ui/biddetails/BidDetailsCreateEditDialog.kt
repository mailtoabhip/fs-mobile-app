package com.delhivery.axle.ui.biddetails

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.delhivery.axle.R
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.DialogBidCreateEditBinding
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.errorVibrate
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
/**
 * Bid Create/Edit dialog
 */
class BidDetailsCreateEditDialog @Inject constructor(
        context: Context,
        private val transaction: HomeBidsRequestItemData,
        private val transactionBid: TransactionBid? = null, /* transaction bid null for create new bid */
        private val dialogInterface: BidDetailsCreateEditDialogInterface,
        private val position: Int = 0,
        private val analyticsUtil: AnalyticsUtil,
        private var userPrefs: UserPrefs,
        private var fromPage: String
) : AlertDialog(context) {

  /* dialog binding */
  private lateinit var binding: DialogBidCreateEditBinding
  private var amount = 0
  private var pmtRate = 0
  private var isChecked = false
  private var expectedArrivalTimePickup = ""
  private var expectedArrivalTimePickupRemark = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
    )

    setCancelable(false)

    /* dialog binding */
    binding = DialogBidCreateEditBinding.inflate(layoutInflater)
    setContentView(binding.root)

    /* set binding params */
    binding.apply {
      request = transaction
      route = transaction.tripRoute()

      binding.repTime.hint = context.getString(R.string.hint_rep_time)

      if (transaction.isPMTIndent()) {
        binding.tilAmount.hint = context.getString(R.string.hint_enter_pmt_rate_value)
        transactionBid?.bidAmount?.let {
          binding.tilAmount.editText?.setText(DecimalFormat("#########").format(it))
        }
        transactionBid?.pmtRate?.let {
          binding.labelBid.text =
                  "Your minimum payout will be ₹${StringUtils.formatAmount(transactionBid.pmtRate)}"
        }
      } else {
        binding.tilAmount.hint = context.getString(R.string.hint_enter_bid_value)
        transactionBid?.bidAmount?.let {
          binding.tilAmount.editText?.setText(DecimalFormat("#########").format(it))
        }
      }
    }

    val items= context.resources.getStringArray(R.array.reportingTimeItems)

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
    binding.editTime.adapter = spinnerAdapter

    binding.editTime.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
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
        }
      }

    }

    binding.tilAmount.editText?.addTextChangedListener(object : TextWatcher {
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
          binding.tilAmount.error = null
          binding.tilAmount.isErrorEnabled = false
          try {
            val input = s.trim()
                    .toString()
                    .toInt()
            if (transaction.isPMTIndent()) {
              pmtRate = input
              if (pmtRate > userPrefs.maxPMTRate) {
                throw Exception("*Rate should be less than ${userPrefs.maxPMTRate}/MT")
              }
              amount = (input * transaction.requestedCapacityMg).toInt()
              binding.labelBid.text = "Your minimum payout will be ₹ $amount"

              if (transactionBid != null) {
                if (abs((input * transaction.requestedCapacityMg) - (transactionBid?.pmtRate
                                ?: 0.0)) < 500) {
                  throw Exception("*Bid difference should be more than ₹500")
                }
              }
            } else {
              amount = input
            }
          } catch (e: NumberFormatException) {
            binding.tilAmount.isErrorEnabled = true
            binding.tilAmount.error = "*Invalid Value"
            amount = 0
            binding.labelBid.text = ""
          } catch (e: Exception) {
            binding.tilAmount.isErrorEnabled = true
            binding.tilAmount.error = e.message
            amount = 0
          }
        }
      }
    })

    if(transactionBid?.expectedArrivalTimePickupRemark.isNotNullOrEmpty()){
      val text = transactionBid?.expectedArrivalTimePickupRemark
      if(text.equals("Immediately")) {
         binding.editTime.setSelection(1)
      }else if(text.equals("Within 4 hours")) {
        binding.editTime.setSelection(2)
      }else if(text.equals("Between 4-12 hours")) {
        binding.editTime.setSelection(3)
      }else if(text.equals("Tomorrow")) {
        binding.editTime.setSelection(4)
      }
    }

    binding.btnConfirm.setOnClickListener {
      binding.editAmount.clearFocus()
      if(getReportingTime()) {
        submit()
      }
    }

    binding.btnCancel.setOnClickListener { dismiss() }
  }

  private fun submit() {
    try {
      require(
              !(transaction.isPMTIndent() && pmtRate > userPrefs.maxPMTRate)
      ) { "*Rate should be less than ${userPrefs.maxPMTRate}/MT" }
      if (amount > 0) {
        if (transaction.isPMTIndent()) {
          val costPerKm = pmtRate / transaction.distance
          if (costPerKm > userPrefs.maxCostPerKM && !isChecked) {
            isChecked = true
            throw IllegalArgumentException(
                    "*Are you sure you want to bid ₹ ${
                      StringUtils.formatDecimalAmount(
                              costPerKm
                      )
                    } /MT/KM"
            )
          }
        } else require(
                !(transactionBid?.bidAmount != null && abs(transactionBid.bidAmount - amount) < 500)
        ) { "*Bid difference should be more than ₹500" }
        if (transactionBid == null) {
          analyticsUtil.trackEvent(
                  EVENT_PLACE_BID,
                  mutableListOf(PROPERTY_USER_ID, PROPERTY_TRANSACTION_ID, PROPERTY_DEMAND_TYPE, PROPERTY_TIME_LAPSE, PROPERTY_OVERALL_PERFORMANCE, PROPERTY_PAGE_NAME),
                  mutableListOf(userPrefs.userId(), transaction.key(), userPrefs.demandType, transaction.timeLapse(), userPrefs.userPerformance, fromPage)
          )
          dialogInterface.createBid(
                  transaction.isPMTIndent(), transaction.key(), amount, pmtRate,
                  transaction.biddingType
                          ?: "FTL", position, expectedArrivalTimePickup, expectedArrivalTimePickupRemark
          )
        } else {
          var event: String? = null
          if (transaction.layoutOneVisibility() || transaction.layoutTwoVisibility()) {
            event = EVENT_EDIT_BID
          } else if (transaction.layoutThreeVisibility() || transaction.layoutFourVisibility()) {
            event = EVENT_REVISE_BID_INTENT
          }
          // Capture event
          if (event != null) {
            analyticsUtil.trackEvent(
                    event,
                    mutableListOf(PROPERTY_USER_ID, PROPERTY_TRANSACTION_ID, PROPERTY_DEMAND_TYPE, PROPERTY_OVERALL_PERFORMANCE, PROPERTY_PAGE_NAME),
                    mutableListOf(userPrefs.userId(), transaction.key(), userPrefs.demandType, userPrefs.userPerformance, fromPage)
            )
          }
          dialogInterface.editBid(
                  transaction.isPMTIndent(), transaction.key(), transactionBid.key(),
                  amount, pmtRate, transaction.biddingType ?: "FTL", position,
                  expectedArrivalTimePickup, expectedArrivalTimePickupRemark
          )
        }
        dismiss()
      } else {
        throw IllegalArgumentException("*Invalid amount")
      }
    } catch (e: IllegalArgumentException) {
      binding.tilAmount.isErrorEnabled = true
      binding.tilAmount.error = e.message
      val shake = AnimationUtils.loadAnimation(context, R.anim.shake)
      binding.tilAmount.startAnimation(shake)
    }
  }


  fun getReportingTime():Boolean {
    val text: String = binding.editTime.selectedItem.toString()
    if(text.equals("Select Time")){
      binding.editTime.errorVibrate(true)
      return false
    }else{
      val myDate = Date()
      val calendar: Calendar = Calendar.getInstance()
      calendar.setTimeZone(TimeZone.getTimeZone("UTC"))
      calendar.setTime(myDate)
      if(text.equals("Immediately")) {
        calendar.add(Calendar.MINUTE,30);
      }else if(text.equals("Within 4 hours")) {
        calendar.add(Calendar.HOUR, 4);
      }else if(text.equals("Between 4-12 hours")) {
        calendar.add(Calendar.HOUR, 12);
      }else if(text.equals("Tomorrow")) {
        calendar.add(Calendar.HOUR, 24);
      }
      val time: Date = calendar.getTime()
      val outputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")
      outputFmt.setTimeZone(TimeZone.getTimeZone("UTC"))
      val dateAsString: String = outputFmt.format(time)
      expectedArrivalTimePickup = dateAsString
      expectedArrivalTimePickupRemark = text
      return true
    }
  }
}

interface BidDetailsCreateEditDialogInterface {

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
          expectedArrivalTimePickup: String,
          expectedArrivalTimePickupRemark: String
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
          expectedArrivalTimePickup: String,
          expectedArrivalTimePickupRemark: String
  )
}