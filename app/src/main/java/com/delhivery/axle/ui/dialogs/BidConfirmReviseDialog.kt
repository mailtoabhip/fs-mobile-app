package com.delhivery.axle.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.DialogConfirmBidBinding
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
      btnDone.setOnClickListener { dismiss() }
      btnRevise.setOnClickListener { reviseBidDialog() }
    }
  }

  /**
   * Revise bid dialog
   */
  private fun reviseBidDialog() {
    dialogInterface.reviseBid(position)
    dismiss()
  }
}


interface BidConfirmReviseDialogInterface {
  fun reviseBid(position: Int)
}