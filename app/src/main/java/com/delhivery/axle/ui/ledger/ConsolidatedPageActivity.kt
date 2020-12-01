package com.delhivery.axle.ui.ledger

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.delhivery.axle.R
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemAction
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemData
import com.delhivery.axle.data.ledger.ConsolidatedMonthItemAction
import com.delhivery.axle.data.ledger.ConsolidatedMonthItemData
import com.delhivery.axle.databinding.ActivityConsolidatedPageBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.MonthDialog
import com.delhivery.axle.ui.dialogs.MonthSelectorDialog
import com.delhivery.axle.ui.dialogs.YearDialog
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar


class ConsolidatedPageActivity: BaseActivity<ActivityConsolidatedPageBinding, ConsolidatedPageViewModel>(), ConsolidatedPageRVAdapterInterface, NavigationView.OnNavigationItemSelectedListener, MonthDialog.MonthDialogListener, YearDialog.YearDialogListener {
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
            refreshData()
        }

        binding.rvConsolidatedLedger.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@ConsolidatedPageActivity.adapter
        }

        if(viewModel.isLoadedNow){
            var dialog = MonthDialog()
            dialog.show(supportFragmentManager, "MonthDialog")
            viewModel.isLoadedNow = false

        }
        ///viewModel.initiateMonths()

//        viewModel.loadsLiveData.observe(this, Observer {
//            binding.error = false
//            it?.let { _items ->
//                adapter.operation(_items)
//            }
//        })

    }

    override fun onItemClicked(item: BaseConsolidatedPageRVAdapterItem<*>, position: Int) {
        TODO("Not yet implemented")
    }

    override fun onMonthClick(selectedMonth: Int) {
        uiUtils.showSnackbar(""+selectedMonth,Snackbar.LENGTH_LONG)
        viewModel.selectedMonth = selectedMonth

        var dialog = YearDialog()
        dialog.show(supportFragmentManager,"YearDialog")
    }

    override fun onYearClick(selectedYear: Int) {
        viewModel.selectedYear = selectedYear
        viewModel.initiateLedgerData()
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

    private fun refreshData(){

    }
}

private const val RandomKey = "RandomKey"

fun consolidatedPageIntent(
        context: Context
) = Intent(context, ConsolidatedPageActivity::class.java).apply {
    putExtra(RandomKey, "Hello")
}
