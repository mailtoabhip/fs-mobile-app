package com.delhivery.axle.ui.biddetails

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import com.delhivery.axle.R
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.DialogBidCreateEditBinding
import com.delhivery.axle.utils.AnalyticsUtil
import com.delhivery.axle.utils.EVENT_EDIT_BID
import com.delhivery.axle.utils.EVENT_PLACE_BID
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_ID
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.prefs.UserPrefs
import java.text.DecimalFormat
import javax.inject.Inject

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
  private var userPrefs: UserPrefs
) : AlertDialog(context) {

  /* dialog binding */
  private lateinit var binding: DialogBidCreateEditBinding
  private var amount = 0
  private var pmtRate = 0
  private var isChecked = false

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
      if (transaction.isPMTIndent()) {
        binding.tilAmount.hint = context.getString(R.string.hint_enter_pmt_rate_value)
        transactionBid?.bidAmount.let {
          binding.tilAmount.editText?.setText(DecimalFormat("#########").format(it))
        }
        transactionBid?.pmtRate?.let {
          binding.labelBid.text = "Your bid is: ${StringUtils.formatAmount(transactionBid.pmtRate)}"
        }
      } else {
        binding.tilAmount.hint = context.getString(R.string.hint_enter_bid_value)
        transactionBid?.bidAmount?.let {
          binding.tilAmount.editText?.setText(DecimalFormat("#########").format(it))
        }
      }
    }

    binding.tilAmount.editText?.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) {
        // Do nothing
      }

      override fun beforeTextChanged(
        s: CharSequence?,
        start: Int,
        count: Int,
        after: Int
      ) {
        // Do nothing
      }

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
            amount = if (transaction.isPMTIndent()) {
              pmtRate = input
              (input * transaction.requestedCapacityMg).toInt()
            } else {
              input
            }
            if (pmtRate > userPrefs.maxPMTRate) {
              throw Exception("*Rate should be less than ${userPrefs.maxPMTRate}/MT")
            }
            binding.labelBid.text = "Your bid is: ₹ $amount"
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

    binding.btnConfirm.setOnClickListener {
      binding.editAmount.clearFocus()
      submit()
    }

    binding.btnCancel.setOnClickListener { dismiss() }
  }

  /**
   * Submit amount
   */
  private fun submit() {
    try {
      require(
          !(transaction.isPMTIndent() && pmtRate > userPrefs.maxPMTRate)
      ) { "*Rate should be less than ${userPrefs.maxPMTRate}/MT" }
      if (amount > 0) {
        val costPerKm = pmtRate / transaction.distance
        if (costPerKm > userPrefs.maxCostPerKM && !isChecked && transaction.isPMTIndent()) {
          isChecked = true
          throw IllegalArgumentException(
              "*Are you sure to bid at ₹ ${StringUtils.formatDecimalAmount(costPerKm)} /MT/KM"
          )
        }
        val event: String
        if (transactionBid == null) {
          event = EVENT_PLACE_BID
          dialogInterface.createBid(
              transaction.isPMTIndent(), transaction.key(), amount, pmtRate,
              transaction.commercialType ?: "", position
          )
        } else {
          event = EVENT_EDIT_BID
          dialogInterface.editBid(
              transaction.isPMTIndent(), transaction.key(), transactionBid.key(),
              amount, pmtRate, transaction.commercialType ?: "", position
          )
        }
        // Capture event
        analyticsUtil.trackEvent(
            event,
            mutableListOf(PROPERTY_TRANSACTION_ID),
            mutableListOf(transaction.key())
        )
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
    position: Int = -1
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
    position: Int = -1
  )
}