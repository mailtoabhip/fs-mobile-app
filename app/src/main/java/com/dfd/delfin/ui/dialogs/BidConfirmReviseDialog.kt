package com.dfd.delfin.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import com.dfd.delfin.data.bids.TransactionBid
import com.dfd.delfin.data.home.bids.HomeBidsRequestItemData
import com.dfd.delfin.databinding.DialogConfirmBidBinding
import com.dfd.delfin.utils.*
import javax.inject.Inject

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 26/4/21
 */
class BidConfirmReviseDialog @Inject constructor(
  context : Context,
  private val transaction: HomeBidsRequestItemData,
  private val dialogInterface: BidConfirmReviseDialogInterface,
  private val position: Int = 0
) : AlertDialog(context) {

  /* dialog binding */
  private lateinit var binding: DialogConfirmBidBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setCancelable(false)

    /* dialog binding */
    binding = DialogConfirmBidBinding.inflate(layoutInflater)
    setContentView(binding.root)

    /* set binding params */
    binding.apply {
      request = transaction
      closeBtn1.setOnClickListener { dismiss() }
      closeBtn2.setOnClickListener { dismiss() }
      closeBtn3.setOnClickListener { dismiss() }
      closeBtn4.setOnClickListener { dismiss() }
      btnContinue1.setOnClickListener { dismiss() }
      btnContinue2.setOnClickListener { dismiss() }
      btnContinue3.setOnClickListener { dismiss() }
      btnContinue4.setOnClickListener { dismiss() }
      btnRevise3.setOnClickListener { reviseBidDialog(transaction.transactionBid) }
      btnRevise4.setOnClickListener { reviseBidDialog(transaction.transactionBid) }
    }
  }

  /**
   * Revise bid dialog
   */
  private fun reviseBidDialog(transactionBid: TransactionBid?) {
    dialogInterface.reviseBid(transaction.transactionBid,position)
    dismiss()
  }
}


interface BidConfirmReviseDialogInterface {
  fun reviseBid(transactionBid: TransactionBid?,position: Int)
}

