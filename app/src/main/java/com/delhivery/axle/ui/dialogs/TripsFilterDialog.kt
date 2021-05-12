package com.delhivery.axle.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import com.delhivery.axle.databinding.DialogTripsFilterBinding

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 12/5/21
 */

class TripsFilterDialog (
  context: Context,
  private val filter1: String,
  private val filter2: String,
    private val dialogInterface: FilterTripsInterface
) : AlertDialog(context), RadioGroup.OnCheckedChangeListener {

  /* dialog binding */
  private lateinit var binding: DialogTripsFilterBinding

  private var optionFilter = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window?.clearFlags(
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
    )

    setCancelable(false)

    /* dialog binding */
    binding = DialogTripsFilterBinding.inflate(layoutInflater)
    setContentView(binding.root)

    /* bind data to layout */
    binding.apply {
      binding.all.text = filter1
      binding.issuedTrips.text = filter2
      radioGroup.check(binding.all.id)
    }

    binding.btnClose.setOnClickListener { dismiss() }

    binding.btnConfirm.setOnClickListener {
      dialogInterface.onConfirmClick(optionFilter)
      dismiss()
    }
  }

  override fun onCheckedChanged(
    p0: RadioGroup?,
    p1: Int
  ) {
    val checkedRadioButton = p0?.findViewById(p0.checkedRadioButtonId) as? RadioButton
    checkedRadioButton?.let {
      if (checkedRadioButton.isChecked) {
        val text = checkedRadioButton.text
        Log.d("trip_filter selected",checkedRadioButton.text.toString())

        optionFilter = when (text) {
          "Trips with issue (34)" -> {
            "issue_trips"
          }
          else -> {
            "all"
          }
        }

      }
    }
  }
}

interface FilterTripsInterface{
  fun onConfirmClick(filter: String)
}