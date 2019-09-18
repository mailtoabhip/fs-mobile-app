package com.delhivery.axle.ui.home.activity.transactiondetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityTransactionDetailBinding
import com.delhivery.axle.ui.base.BaseActivity

class TransactionDetailActivity : BaseActivity<ActivityTransactionDetailBinding, TransactionDetailViewModel>() {

  override fun getViewModelClass() = TransactionDetailViewModel::class.java

  override fun layoutId() = R.layout.activity_home

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
fun transactionDetailIntent(
  context: Context
) = Intent(context, TransactionDetailActivity::class.java).apply {
}