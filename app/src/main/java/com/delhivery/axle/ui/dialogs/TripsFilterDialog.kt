package com.delhivery.axle.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import com.delhivery.axle.databinding.DialogTripsFilterBinding
import com.delhivery.axle.utils.AnalyticsUtil
import com.delhivery.axle.utils.EVENT_FILTER_BALANCE_PENDING
import com.delhivery.axle.utils.PROPERTY_FILTER_SELECTED
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.prefs.UserPrefs

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 12/5/21
 */

class TripsFilterDialog (
  context: Context,
  private val filterList: List<String>,
  private val dialogInterface: FilterTripsInterface,
  private val analyticsUtil: AnalyticsUtil,
  private val userPrefs: UserPrefs,
  private val filterKey : String) : AlertDialog(context) {

  /* dialog binding */
  private lateinit var binding: DialogTripsFilterBinding
  private lateinit var radioButton: RadioButton

  private var optionFilter = ""
  var event=false

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
      when (filterList.size) {
        1 -> {
          binding.filter1.visibility = View.VISIBLE
          binding.filter1.text = filterList[0]

          binding.filter2.visibility = View.GONE
          binding.filter3.visibility = View.GONE
          binding.filter4.visibility = View.GONE
        }
        2 -> {
          if(filterKey == "trips_with_issue") {
            event = true
          }
          binding.filter1.visibility = View.VISIBLE
          binding.filter2.visibility = View.VISIBLE
          binding.filter1.text = filterList[0]
          binding.filter2.text = filterList[1]

          binding.filter3.visibility = View.GONE
          binding.filter4.visibility = View.GONE
        }
        3 -> {
          binding.filter1.visibility = View.VISIBLE
          binding.filter2.visibility = View.VISIBLE
          binding.filter3.visibility = View.VISIBLE
          binding.filter1.text = filterList[0]
          binding.filter2.text = filterList[1]
          binding.filter3.text = filterList[2]

          binding.filter4.visibility = View.GONE
        }
        4 -> {
          binding.filter1.visibility = View.VISIBLE
          binding.filter2.visibility = View.VISIBLE
          binding.filter3.visibility = View.VISIBLE
          binding.filter4.visibility = View.VISIBLE
          binding.filter1.text = filterList[0]
          binding.filter2.text = filterList[1]
          binding.filter3.text = filterList[2]
          binding.filter4.text = filterList[3]
        }
        else -> {
          binding.filter1.visibility = View.GONE
          binding.filter2.visibility = View.GONE
          binding.filter3.visibility = View.GONE
          binding.filter4.visibility = View.GONE
        }
      }

      radioGroup.check(binding.filter1.id)
      binding.btnConfirm.setOnClickListener {
        val selectedOption: Int = binding.radioGroup.checkedRadioButtonId
        radioButton = findViewById(selectedOption)!!

        if(event){
          analyticsUtil.trackEvent(
                  EVENT_FILTER_BALANCE_PENDING,
                  mutableListOf(PROPERTY_USER_ID, PROPERTY_FILTER_SELECTED),
                  mutableListOf(userPrefs.userId(), radioButton.text.toString())
          )
        }

        optionFilter = filterValue(radioButton.text.toString())
        dialogInterface.onConfirmClick(optionFilter)
        dismiss()
      }
    }

    binding.btnClose.setOnClickListener { dismiss() }
  }

  private fun filterValue(text: String = ""): String {
    return when (text) {
      "Less than 1 day" -> {
        "less_than_1_day"
      }
      "1 day +" -> {
        "1_day"
      }
      "2 days +" -> {
        "2_days"
      }
      "3 days +" -> {
        "more_than_3_days"
      }
      "Delayed" -> {
        "delayed"
      }
      "Trips with POD issue" -> {
        "issue_trips"
      }
      else -> {
        "all"
      }
    }
  }
}

interface FilterTripsInterface{
  fun onConfirmClick(filter: String)
}