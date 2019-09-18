package com.delhivery.axle.ui.home.activity.transactionlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityTransactionsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapter

class TransactionsActivity : BaseActivity<ActivityTransactionsBinding, TransactionsViewModel>(),
    TransactionsRVAdapterInterface {

  override fun getViewModelClass() = TransactionsViewModel::class.java

  override fun layoutId() = R.layout.activity_transactions

  override fun requireConnection() = true

  private val adapter: TransactionsRVAdapter by lazy {
    TransactionsRVAdapter(this)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Transactions Summary"

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      refreshData()
    }

    binding.rvTransactions.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@TransactionsActivity.adapter
    }
  }

  private fun refreshData() {

  }

  private fun openFilter() {

  }

  override fun handleAction(
    actionId: String,
    item: BaseTransactionsRVAdapterItem<*>
  ) {

  }

}

/**
 * Transactions intent
 */
fun transactionsIntent(
  context: Context
) = Intent(context, TransactionsActivity::class.java).apply {
}