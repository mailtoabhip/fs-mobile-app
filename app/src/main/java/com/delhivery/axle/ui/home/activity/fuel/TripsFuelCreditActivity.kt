package com.delhivery.axle.ui.home.activity.fuel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.axle.databinding.ActivityTripsFuelCardBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.fuelcard.createFuelCardIntent
import com.delhivery.axle.utils.PaginationScrollListener
import com.delhivery.axle.utils.REQCODE_CREATE_FUELCARD

class TripsFuelCreditActivity : BaseActivity<ActivityTripsFuelCardBinding, TripsFuelCardViewModel>(),
    TripsFuelRVAdapterInterface {

  override fun getViewModelClass() = TripsFuelCardViewModel::class.java

  override fun layoutId() = R.layout.activity_trips_fuel_card

  override fun requireConnection() = true

  var isLoadingData = true

  private val adapter: TripsFuelRVAdapter by lazy {
    TripsFuelRVAdapter(this)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Fuel Cards"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    viewModel.tripsliveData.observe(this, Observer {
      it?.let { _items -> adapter.operation(_items) }
    })

    binding.refreshLayout.setOnRefreshListener {
      refreshData()
    }

    binding.rvTrips.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@TripsFuelCreditActivity.adapter
      addOnScrollListener(PaginationInterface())
    }

    refreshData()
  }

  private fun refreshData() {
    binding.refreshLayout.isRefreshing = false
    adapter.resetStaticData()
    viewModel.fetchFuelCards()
  }

  override fun handleAction(
    actionId: String,
    item: BaseTripsFuelRVAdapterItem<*>
  ) {
    when (actionId) {
      HomeTripsRequestAction_ViewDetails -> {
        val data = item.data as HomeTripsItemData
        navigationUtils.navigateForActivityResult(
            createFuelCardIntent(this, data), false, REQCODE_CREATE_FUELCARD
        )
      }
      else -> {

      }
    }
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQCODE_CREATE_FUELCARD && resultCode == Activity.RESULT_OK) {
      refreshData()
    }
  }

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(10) {
    override fun loadMore() = viewModel.fetchTrips(true)

    override fun hasMore() = viewModel.offset < viewModel.total

    override fun isLoading() = isLoadingData
  }
}

/**
 * Trips FuelCredit intent
 */
fun tripsFuelCreditIntent(
  context: Context
) = Intent(context, TripsFuelCreditActivity::class.java).apply {
}