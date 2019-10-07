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
import com.delhivery.axle.data.transactions.TransactionTimeOutAction
import com.delhivery.axle.data.transactions.TransactionWarningAction_NoTransactions
import com.delhivery.axle.databinding.ActivityActiveTripsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.fuelcard.createFuelCardIntent
import com.delhivery.axle.utils.PaginationScrollListener
import com.delhivery.axle.utils.REQCODE_CREATE_FUELCARD

class ActiveTripsActivity : BaseActivity<ActivityActiveTripsBinding, ActiveTripsViewModel>(),
    ActiveTripsRVAdapterInterface {

  override fun getViewModelClass() = ActiveTripsViewModel::class.java

  override fun layoutId() = R.layout.activity_active_trips

  override fun requireConnection() = true

  var isLoadingData = true

  private val adapter: ActiveTripsRVAdapter by lazy {
    ActiveTripsRVAdapter(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    /* validate intent */
    if (intent == null || !intent.hasExtra(ARGS_OPTIN_DATE)) {
      throw IllegalArgumentException("Required data $ARGS_OPTIN_DATE not found")
    }

    viewModel.optinDate = intent?.getStringExtra(ARGS_OPTIN_DATE) ?: ""
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Select Active Trip for fuel"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    viewModel.dataLoadingLiveData.observe(this, Observer {
      isLoadingData = it ?: false
    })

    viewModel.tripsLiveData.observe(this, Observer {
      it?.let { _items -> adapter.operation(_items) }
    })

    binding.refreshLayout.setOnRefreshListener {
      refreshData()
    }

    binding.rvTrips.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@ActiveTripsActivity.adapter
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
    item: BaseActiveTripsRVAdapterItem<*>
  ) {
    when (actionId) {
      HomeTripsRequestAction_ViewDetails -> {
        val data = item.data as HomeTripsItemData
        navigationUtils.navigateForActivityResult(
            createFuelCardIntent(this, data), false, REQCODE_CREATE_FUELCARD
        )
      }

      TransactionTimeOutAction -> {
        refreshData()
      }

      TransactionWarningAction_NoTransactions -> {
        setResult(RESULT_OK)
        finish()
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

/* intent keys */
private const val ARGS_OPTIN_DATE = "args_optin_date"

/**
 * Trips Fuel Credit intent
 */
fun tripsFuelCreditIntent(
  context: Context,
  optinDate: String
) = Intent(context, ActiveTripsActivity::class.java).apply {
  putExtra(ARGS_OPTIN_DATE, optinDate)
}