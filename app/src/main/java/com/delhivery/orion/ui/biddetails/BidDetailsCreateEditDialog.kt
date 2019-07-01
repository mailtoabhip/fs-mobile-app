package com.delhivery.orion.ui.biddetails

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import android.view.animation.AnimationUtils
import com.delhivery.orion.R
import com.delhivery.orion.data.bids.TransactionBid
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.databinding.DialogBidCreateEditBinding
import javax.inject.Inject

/**
 *
 */
class BidDetailsCreateEditDialog @Inject constructor(
  context: Context,
  private val transaction: HomeBidsRequestItemData,
  private val transactionBid: TransactionBid? = null, /* transaction bid null for create new bid */
  private val dialogInterface: BidDetailsCreateEditDialogInterface,
  private val position: Int = 0
) : AlertDialog(context) {

  /* dialog binding */
  private lateinit var binding: DialogBidCreateEditBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.clearFlags(
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
    )

    setCancelable(false)

    /* dialog binding */
    binding = DialogBidCreateEditBinding.inflate(layoutInflater)
    setContentView(binding.root)

    /* set binding params */
    binding.apply {
      targetPrice = transaction.targetPrice
      route = "${transaction.origin} - ${transaction.destination}"
      transactionBid?.bidAmount?.let { binding.editAmount.setText(it.toString()) }
    }

    /* button click listeners */
    binding.btnConfirm.setOnClickListener { submit() }
    binding.btnCancel.setOnClickListener { dismiss() }
  }

  /**
   * Submit amount
   */
  private fun submit() {
    try {
      val _amount = Integer.parseInt(binding.editAmount.text.toString())
      if (_amount > 0) {
        if (transactionBid == null) {
          dialogInterface.createBid(transaction.key(), _amount, position)
        } else {
          dialogInterface.editBid(transaction.key(), transactionBid.key(), _amount, position)
        }
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