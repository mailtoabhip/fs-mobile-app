package com.delhivery.axle.ui.biddetails

import android.content.Context
import android.os.Bundle
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
import javax.inject.Inject

/**
 *
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
      route = "${transaction.originCityName()} - ${transaction.destinationCityName()}"
      transactionBid?.bidAmount?.let { binding.editAmount.setText(StringUtils.formatAmount(it)) }
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
      val amount = Integer.parseInt(binding.editAmount.text.toString())
      if (amount > 0) {
        var event = ""
        if (transactionBid == null) {
          event = EVENT_PLACE_BID
          dialogInterface.createBid(transaction.key(), amount, position)
        } else {
          event = EVENT_EDIT_BID
          dialogInterface.editBid(transaction.key(), transactionBid.key(), amount, position)
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
    transactionId: String,
    bidAmount: Int,
    position: Int = -1
  )

  /**
   * Edit bid
   */
  fun editBid(
    transactionId: String,
    bidId: String,
    bidAmount: Int,
    position: Int = -1
  )
}