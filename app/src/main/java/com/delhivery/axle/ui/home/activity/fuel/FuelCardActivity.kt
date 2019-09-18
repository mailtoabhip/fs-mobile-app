package com.delhivery.axle.ui.home.activity.fuel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityFuelCardBinding
import com.delhivery.axle.ui.base.BaseActivity

class FuelCardActivity : BaseActivity<ActivityFuelCardBinding, FuelCardViewModel>() {

  override fun getViewModelClass() = FuelCardViewModel::class.java

  override fun layoutId() = R.layout.activity_fuel_card

  override fun requireConnection() = true

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Transactions Summary"

  }

}

/**
 * Transaction Detail intent
 */
fun fuelCardIntent(
  context: Context
) = Intent(context, FuelCardActivity::class.java).apply {
}