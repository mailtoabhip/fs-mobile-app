package com.delhivery.axle.ui.home.activity.fuelcard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.data.fuelcards.FuelCardData
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.databinding.ActivityCreateFuelCardBinding
import com.delhivery.axle.ui.base.BaseActivity
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
    viewModel.fuelCard = intent.getSerializableExtra(ARGS_FUEL_CARD) as FuelCardData
    viewModel.balance = intent.getIntExtra(ARGS_WALLET_BALANCE, 0)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Create fuel card"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)


    binding.btnCreate.setOnClickListener { createFuelCard() }
  }

  private fun createFuelCard() {
    var mobileNum: String
    when {
      binding.radioDriverNum.isChecked -> {
        mobileNum = viewModel.trip.driverDetails?.driverPhoneNo ?: ""
      }

      binding.radioOtherNum.isChecked -> {
        mobileNum = binding.editMobile.text.toString()
        if (mobileNum.isEmpty() || mobileNum.length != 10) {
          mobileNum = ""
          binding.tilAmount.requestFocus()
          binding.tilAmount.error = "Enter a valid mobile number"
          return
        }
      }
      else -> {
        mobileNum = ""
        uiUtils.showToast("Please select number to recharge")
        return
      }
    }

    val amount = binding.editAmount.text.toString()
    if (amount.isEmpty()) {
      binding.tilAmount.requestFocus()
      return
    } else {
      val mAmount =
        try {
          amount.toInt()
        } catch (e: Exception) {
          binding.tilAmount.requestFocus()
          binding.tilAmount.error = "Enter valid amount"
          return
        }

      val minVal = min(viewModel.trip.bidDetails?.bidPrice ?: 0, viewModel.balance)
      if (mAmount > minVal) {
        binding.tilAmount.requestFocus()
        binding.tilAmount.error = "Max amount which can be transferred is $minVal"
        return
      }
    }

    viewModel.createFuelCard(
        mobileNum, viewModel.trip.vehicleDetails.vehicleNo, amount, viewModel.trip.transactionId
    )
  }

}

/* intent keys */
private const val ARGS_TRIP_DATA = "args_trip_data"
private const val ARGS_FUEL_CARD = "args_fuel_card"
private const val ARGS_WALLET_BALANCE = "args_wallet_balance"

/**
 * Create Fuel Card intent
 */
fun createFuelCardIntent(
  context: Context,
  trip: HomeTripsItemData,
  fuelCard: FuelCardData,
  balance: Int
) = Intent(context, CreateFuelCardActivity::class.java).apply {
  putExtra(ARGS_TRIP_DATA, trip)
  putExtra(ARGS_FUEL_CARD, fuelCard)
  putExtra(ARGS_WALLET_BALANCE, balance)
}