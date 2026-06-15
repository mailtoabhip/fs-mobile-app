package com.dfd.delfin.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import com.dfd.delfin.databinding.DialogLedgerSuccessBinding

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 7/5/21
 */

class LedgerSuccessDialog (
  context: Context
) : AlertDialog(context) {

  /* dialog binding */
  private lateinit var binding: DialogLedgerSuccessBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window?.clearFlags(
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
    )

    setCancelable(false)

    /* dialog binding */
    binding = DialogLedgerSuccessBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.btnClose.setOnClickListener { dismiss() }
  }

}