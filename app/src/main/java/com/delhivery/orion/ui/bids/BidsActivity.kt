package com.delhivery.orion.ui.bids

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.orion.R
import com.delhivery.orion.databinding.ActivityBidsBinding
import com.delhivery.orion.ui.base.BaseActivity

class BidsActivity : BaseActivity<ActivityBidsBinding, BidsViewModel>() {

  override fun getViewModelClass() = BidsViewModel::class.java

  override fun layoutId() = R.layout.activity_bids

  override fun requireConnection() = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (intent == null || !intent.hasExtra(IntentExtraBidTypeKey)) {
      throw IllegalArgumentException("$IntentExtraBidTypeKey intent key missing")
    }

    /* get bid type from intent */
    viewModel.bidType =
        BidType.byTypeId(intent.getIntExtra(IntentExtraBidTypeKey, BidType.Unknown.typeId))
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = viewModel.bidType.toolbarTitle()
  }
}

/*  */
private const val IntentExtraBidTypeKey = "bid_type"

/**
 * Get [BidsActivity] for specific [BidType] as [type]
 */
fun userBidsIntent(
  context: Context,
  type: BidType
) = Intent(context, BidsActivity::class.java).apply {
  putExtra(IntentExtraBidTypeKey, type.typeId)
}