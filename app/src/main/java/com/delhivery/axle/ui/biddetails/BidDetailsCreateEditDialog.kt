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
  private val analyticsUtil: AnalyticsUtil
) : AlertDialog(context) {

  /* dialog binding */
  private lateinit var binding: DialogBidCreateEditBinding
  private var amount = 0

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
        binding.labelValue.text = context.getString(R.string.hint_enter_pmt_rate_value)
      } else {
        binding.labelValue.text = context.getString(R.string.hint_enter_bid_value)
        transactionBid?.bidAmount?.let {
          binding.editAmount.setText(
              DecimalFormat("#########").format(it)
          )
        }
      }
    }

    if (transaction.isPMTIndent()) {
      binding.editAmount.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(
          s: CharSequence?,
          start: Int,
          count: Int,
          after: Int
        ) {

        }

        override fun onTextChanged(
          s: CharSequence?,
          start: Int,
          before: Int,
          count: Int
        ) {
          if (s != null) {
            try {
              val input = s.trim()
                  .toString()
                  .toDouble()
              amount = if (transaction.isPMTIndent()) {
                (input * transaction.requestedCapacityMg).toInt()
              } else {
                input.toInt()
              }
              binding.labelBid.text = "Your amount: ₹ $amount"
            } catch (ne: NumberFormatException) {
              amount = 0
              binding.labelBid.text = ""
            }
          }
        }

      })
    }

    /* button click listeners */
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
      if (amount > 0) {
        val event: String
        if (transactionBid == null) {
          event = EVENT_PLACE_BID
          dialogInterface.createBid(
              transaction.isPMTIndent(), transaction.key(), amount, position
          )
        } else {
          event = EVENT_EDIT_BID
          dialogInterface.editBid(
              transaction.isPMTIndent(), transaction.key(), transactionBid.key(),
              amount, position
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
        throw Exception()
      }
    } catch (e: Exception) {
      val shake = AnimationUtils.loadAnimation(context, R.anim.shake)
      binding.editAmount.startAnimation(shake)
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
    position: Int = -1
  )
}