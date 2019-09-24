package com.delhivery.axle.ui.home.activity.fuel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityTripsFuelCardBinding
import com.delhivery.axle.ui.base.BaseActivity

class TripsFuelCreditActivity : BaseActivity<ActivityTripsFuelCardBinding, TripsFuelCardViewModel>(),
    TripsFuelRVAdapterInterface {

  override fun getViewModelClass() = TripsFuelCardViewModel::class.java

  override fun layoutId() = R.layout.activity_trips_fuel_card

  override fun requireConnection() = true

  private val adapter: TripsFuelRVAdapter by lazy {
    TripsFuelRVAdapter(this)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Fuel"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

//    viewModel.tripsLiveData.observe(this, Observer {
//      it?.let { _items ->
//        adapter.operation(_items)
//      }
//    })

    binding.refreshLayout.setOnRefreshListener {
      refreshData()
    }

    binding.rvTrips.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@TripsFuelCreditActivity.adapter
    }

    refreshData()
  }

  private fun refreshData() {
    adapter.resetStaticData()
//    viewModel.fetchTrips()
  }

  override fun handleAction(
    actionId: String,
    item: BaseTripsFuelRVAdapterItem<*>
  ) {

  }

}

/**
 * Trips FuelCredit intent
 */
fun tripsFuelCreditIntent(
  context: Context
) = Intent(context, TripsFuelCreditActivity::class.java).apply {
}