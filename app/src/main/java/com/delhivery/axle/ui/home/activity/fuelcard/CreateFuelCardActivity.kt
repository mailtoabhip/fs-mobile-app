package com.delhivery.axle.ui.home.activity.fuelcard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.databinding.ActivityCreateFuelCardBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.transactionlist.transactionsIntent
import kotlin.math.min

class CreateFuelCardActivity : BaseActivity<ActivityCreateFuelCardBinding, CreateFuelCardViewModel>() {

  override fun getViewModelClass() = CreateFuelCardViewModel::class.java

  override fun layoutId() = R.layout.activity_create_fuel_card

  override fun requireConnection() = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    /* validate intent */
    if (intent == null || !intent.hasExtra(ARGS_TRIP_DATA)) {
      throw IllegalArgumentException("Required data $ARGS_TRIP_DATA not found")
    }

    viewModel.trip = intent.getSerializableExtra(ARGS_TRIP_DATA) as HomeTripsItemData
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Create fuel card"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    binding.trip = viewModel.trip
    binding.fetching = true
    binding.error = false
    binding.created = false
    binding.createdError = false
    if (viewModel.trip.fuelCard != null && viewModel.trip.fuelCard?.amount != "0") {
      binding.containerActiveCard.visibility = View.VISIBLE
    }
    binding.executePendingBindings()

    viewModel.walletLiveData.observe(this, Observer {
      binding.fetching = false
      if (it != null) {
        viewModel.balance = it.balance.toInt()
        binding.error = false
      } else {
        binding.error = true
      }
      binding.executePendingBindings()
    })

    viewModel.transactionLiveData.observe(this, Observer {
      binding.created = !it.isNullOrEmpty()
      binding.createdError = it.isNullOrEmpty()
      binding.executePendingBindings()
    })

    binding.radioNumGroup.setOnCheckedChangeListener { group, checkedId ->
      when (checkedId) {
        R.id.radio_driver_num -> {
          uiUtils.toggleKeyboard()
          binding.editMobile.clearFocus()
          binding.tilMobile.hint = getString(R.string.label_enter_number)
        }

        R.id.radio_other_num -> {
          binding.editMobile.requestFocus()
        }
      }
    }

    binding.btnRetry.setOnClickListener { createFuelCard() }
    binding.btnCreate.setOnClickListener { createFuelCard() }
    binding.btnTransactionSummary.setOnClickListener {
      navigationUtils.navigate(transactionsIntent(this), true)
    }
    binding.btnBack.setOnClickListener {
      setResult(Activity.RESULT_OK)
      finish()
    }
    binding.containerCreateCardError.setOnClickListener {
      binding.createdError = false
      binding.executePendingBindings()
    }

    binding.fetching = true
    viewModel.fetchWalletData()
  }

  private fun createFuelCard() {
    binding.error = false
    binding.createdError = false
    binding.created = false
    binding.executePendingBindings()
    val mobileNum: String
    if (binding.radioDriverNum.isChecked) {
      mobileNum = viewModel.trip.driverDetails?.driverPhoneNo ?: ""
    } else if (binding.radioOtherNum.isChecked) {
      mobileNum = binding.editMobile.text.toString()
      if (mobileNum.isEmpty() || mobileNum.length != 10) {
        binding.tilMobile.requestFocus()
        binding.tilMobile.error = "Enter a valid mobile number"
        return
      }
    } else {
      uiUtils.showToast("Please select number to recharge")
      return
    }

    val amount = binding.editAmount.text.toString()
    if (amount.isEmpty()) {
      binding.tilAmount.requestFocus()
      return
    } else {
      val mAmount =
        try {
          amount.toDouble()
        } catch (e: Exception) {
          binding.tilAmount.requestFocus()
          binding.tilAmount.error = "Enter valid amount"
          return
        }

      val minVal =
        min(viewModel.trip.bidDetails?.bidPrice?.times(60)?.div(100) ?: 0, viewModel.balance)
      if (mAmount > minVal) {
        binding.tilAmount.requestFocus()
        binding.tilAmount.error = "Max amount which can be transferred is $minVal"
        return
      }
      binding.cashback =
        "You will receive cashback of " + (mAmount * 3 / 100) + " when the trip ends"
    }

    binding.numberRecharged = mobileNum
    binding.amountAdded = "₹ $amount"
    viewModel.createFuelCard(
        mobileNum, viewModel.trip.vehicleDetails.vehicleNo, amount, viewModel.trip.transactionId
    )
  }

}

/* intent keys */
private const val ARGS_TRIP_DATA = "args_trip_data"

/**
 * Create Fuel Card intent
 */
fun createFuelCardIntent(
  context: Context,
  trip: HomeTripsItemData
) = Intent(context, CreateFuelCardActivity::class.java).apply {
  putExtra(ARGS_TRIP_DATA, trip)
}