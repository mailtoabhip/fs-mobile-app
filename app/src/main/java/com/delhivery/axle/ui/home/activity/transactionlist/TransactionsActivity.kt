package com.delhivery.axle.ui.home.activity.transactionlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.data.transactions.TransactionAction_ViewDetails
import com.delhivery.axle.data.transactions.TransactionTimeOutAction
import com.delhivery.axle.data.transactions.TransactionsItemData
import com.delhivery.axle.databinding.ActivityTransactionsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.transactiondetail.transactionDetailIntent
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject

/**
 * Displays wallet transactions
 */
class TransactionsActivity : BaseActivity<ActivityTransactionsBinding, TransactionsViewModel>(),
    TransactionsRVAdapterInterface {

  @Inject lateinit var userPrefs : UserPrefs

  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true

  override fun getViewModelClass() = TransactionsViewModel::class.java

  override fun layoutId() = R.layout.activity_transactions

  override fun requireConnection() = true

  private val adapter: TransactionsRVAdapter by lazy {
    TransactionsRVAdapter(this)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("TransactionsActivity_SetupTime")
    activitySetupTrace?.start()
    setSupportActionBar(binding.toolbar)
    title = "Transaction Summary"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        userPrefs.setPreviousScreen(this.javaClass.name)
        finish()
      }
    })

    viewModel.transactionsLiveData.observe(this, Observer {
      binding.refreshLayout.isRefreshing = false
      it?.let { _items ->
        adapter.operation(_items)
      }
    })

    binding.refreshLayout.setOnRefreshListener {
      refreshData()
    }

    binding.rvTransactions.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@TransactionsActivity.adapter
    }

    refreshData()
  }

  override fun onResume() {
    super.onResume()
    if (activitySetupTrace != null && isFirstResume) {
      activitySetupTrace?.stop()
      isFirstResume = false
    }
  }

  private fun refreshData() {
    binding.refreshLayout.isRefreshing = true
    adapter.resetStaticData()
    viewModel.fetchTransactions()
  }

  override fun handleAction(
    actionId: String,
    item: BaseTransactionsRVAdapterItem<*>
  ) {
    when (actionId) {
      TransactionAction_ViewDetails -> {
        val transaction = item.data as? TransactionsItemData
        navigationUtils.navigate(transactionDetailIntent(this, transaction))
      }

      TransactionTimeOutAction -> {
        refreshData()
      }
    }
  }

}

/**
 * Transactions intent
 */
fun transactionsIntent(
  context: Context
) = Intent(context, TransactionsActivity::class.java).apply {
}