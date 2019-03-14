package com.delhivery.orion.ui.biddetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.orion.R
import com.delhivery.orion.databinding.ActivityBidDetailsBinding
import com.delhivery.orion.ui.base.BaseActivity

class BidDetailsActivity : BaseActivity<ActivityBidDetailsBinding, BidDetailsViewModel>() {
  override fun getViewModelClass() = BidDetailsViewModel::class.java

  override fun layoutId() = R.layout.activity_bid_details

  override fun requireConnection() = true

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "GA - MH (Today)"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }
}

/**
 * Bid details intent
 */
fun bidDetailsIntent(context: Context) = Intent(context, BidDetailsActivity::class.java).apply {
  /* add intent params here */
}