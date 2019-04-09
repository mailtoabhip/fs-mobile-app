package com.delhivery.orion.ui.biddetails

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.orion.R
import com.delhivery.orion.data.home.HomeBidsRequestItemData
import com.delhivery.orion.databinding.ActivityBidDetailsBinding
import com.delhivery.orion.ui.base.BaseActivity

class BidDetailsActivity : BaseActivity<ActivityBidDetailsBinding, BidDetailsViewModel>() {
  override fun getViewModelClass() = BidDetailsViewModel::class.java

  override fun layoutId() = R.layout.activity_bid_details

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

    /* setup live data observers */
    viewModel.transactionLiveData.observe(this, TransactionObserver())

    /* fetch transaction details */
    viewModel.fetchTransactionDetails()
  }

  /**
   * Transaction details and UI updation Observer
   */
  inner class TransactionObserver : Observer<HomeBidsRequestItemData> {
    override fun onChanged(t: HomeBidsRequestItemData?) {
      t?.let { _transaction ->
        binding.transaction = _transaction
        title =
            "${_transaction.originCityCode} - ${_transaction.destinationCityCode} (${_transaction.requiredAt()})"
      }
    }
  }
}

/* intent keys */
private const val TransactionIdIntentKey = "transaction_id"

/**
 * Bid dvetails intent
 */
fun bidDetailsIntent(
  _data: HomeBidsRequestItemData,
  context: Context
) = Intent(context, BidDetailsActivity::class.java).apply {
  putExtra(TransactionIdIntentKey, _data.uuid)
}