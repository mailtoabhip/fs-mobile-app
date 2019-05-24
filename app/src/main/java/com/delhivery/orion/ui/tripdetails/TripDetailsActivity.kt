package com.delhivery.orion.ui.tripdetails

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.orion.R
import com.delhivery.orion.data.TripHistoryModel
import com.delhivery.orion.data.home.trips.HomeTripsItemData
import com.delhivery.orion.databinding.ActivityTripDetailsBinding
import com.delhivery.orion.databinding.ViewTripHistoryItemBinding
import com.delhivery.orion.ui.base.BaseActivity

class TripDetailsActivity : BaseActivity<ActivityTripDetailsBinding, TripDetailsViewModel>() {
  override fun getViewModelClass() = TripDetailsViewModel::class.java

  override fun layoutId() = R.layout.activity_trip_details

  override fun requireConnection() = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    /* validate intent */
    if (intent == null || !intent.hasExtra(TransactionIdIntentKey)) {
      throw IllegalArgumentException("Required data $TransactionIdIntentKey not found")
    }

    /* set transaction id */
    viewModel.transactionId = intent.getStringExtra(TransactionIdIntentKey)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Orion"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)


    /* observe trip details live data */
    viewModel.tripLiveData.observe(this, Observer {
      it?.apply {
        title = first.tripDisplayName()
        binding.transactionDetails = first
        binding.tripDetails = second
        bindHistory(third)
      }
    })

    /* fetch trip details */
    viewModel.tripDetails()
  }

  /**
   * Bind trip history
   */
  private fun bindHistory(history: List<TripHistoryModel>) {
    binding.containerHistory.removeAllViews()
    history.forEach { _item ->
      ViewTripHistoryItemBinding.inflate(layoutInflater, binding.containerHistory, false)
          .apply {
            setHistory(_item)
            binding.containerHistory.addView(root)
          }
    }
  }
}

/* intent keys */
private const val TransactionIdIntentKey = "transaction_id"

/**
 * Trip details intent
 */
fun tripDetailsIntent(
  _data: HomeTripsItemData,
  context: Context
) = Intent(context, TripDetailsActivity::class.java).apply {
  putExtra(TransactionIdIntentKey, _data.key())
}

