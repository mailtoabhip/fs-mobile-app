package com.delhivery.axle.ui.ledger

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.delhivery.axle.databinding.ActivityConsolidatedPageBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.google.android.material.navigation.NavigationView
import com.delhivery.axle.R
import androidx.lifecycle.Observer
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemAction
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemData
import com.delhivery.axle.data.ledger.ConsolidatedMonthItemAction
import com.delhivery.axle.data.ledger.ConsolidatedMonthItemData


class ConsolidatedPageActivity: BaseActivity<ActivityConsolidatedPageBinding, ConsolidatedPageViewModel>(), ConsolidatedPageRVAdapterInterface, NavigationView.OnNavigationItemSelectedListener {
    override fun getViewModelClass() = ConsolidatedPageViewModel::class.java

    override fun layoutId() = R.layout.activity_consolidated_page

    override fun requireConnection() = true

    private val adapter: ConsolidatedPageRVAdapter by lazy { ConsolidatedPageRVAdapter(this) }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Your Money"

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            //Something to be written
        }

        binding.rvConsolidatedLedger.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@ConsolidatedPageActivity.adapter
        }

        binding.isLoadedNow= true


        viewModel.initiateMonths()

        viewModel.loadsLiveData.observe(this, Observer {
            binding.error = false
            it?.let { _items ->
                adapter.operation(_items)
            }
        })

    }

    override fun onItemClicked(item: BaseConsolidatedPageRVAdapterItem<*>, position: Int) {
        TODO("Not yet implemented")
    }

    override fun handleAction(actionId: String, position: Int, item: BaseConsolidatedPageRVAdapterItem<*>) {
        when (actionId){
            ConsolidatedMonthItemAction -> {
                val data = item.data as ConsolidatedMonthItemData
                adapter.toggle(position,data)
            }

            ConsolidatedLedgerItemAction -> {
                val data = item.data as ConsolidatedLedgerItemData
                uiUtils.showSnackbar("Action Item Clicked $data")
            }
        }
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        TODO("Not yet implemented")
    }
}

private const val RandomKey = "RandomKey"

fun consolidatedPageIntent(
        context: Context
) = Intent(context, ConsolidatedPageActivity::class.java).apply {
    putExtra(RandomKey, "Hello")
}
