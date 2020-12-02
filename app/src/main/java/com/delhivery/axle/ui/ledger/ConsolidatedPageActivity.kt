package com.delhivery.axle.ui.ledger

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.UserTripsLoadLimit
import com.delhivery.axle.data.ledger.*
import com.delhivery.axle.databinding.ActivityConsolidatedPageBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.DownloadLedgerDialog
import com.delhivery.axle.ui.dialogs.MonthDialog
import com.delhivery.axle.ui.dialogs.YearDialog
import com.delhivery.axle.utils.PaginationScrollListener
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*


class ConsolidatedPageActivity: BaseActivity<ActivityConsolidatedPageBinding, ConsolidatedPageViewModel>(),
        ConsolidatedPageRVAdapterInterface,
        NavigationView.OnNavigationItemSelectedListener,
        MonthDialog.MonthDialogListener,
        YearDialog.YearDialogListener{
    override fun getViewModelClass() = ConsolidatedPageViewModel::class.java

    override fun layoutId() = R.layout.activity_consolidated_page

    override fun requireConnection() = true

    private val adapter: ConsolidatedPageRVAdapter by lazy { ConsolidatedPageRVAdapter(this) }
    private val ledgerSpinnerAdapter: LedgerSpinnerAdapter by lazy { LedgerSpinnerAdapter() }

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
            addOnScrollListener(PaginationInterface())
        }

        binding.textRequestStatement.setOnClickListener{
            val dialog = DownloadLedgerDialog(this)
            dialog.setOwnerActivity(this)
            if (!this.isFinishing)
                dialog.show()
        }

        binding.spinnerShowing.apply {
            adapter = ledgerSpinnerAdapter
            ledgerSpinnerAdapter.setItems(
                    LedgerSpinnerOptions.values().toList()
            )
            setSelection(0)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>) = Unit
                override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View,
                        position: Int,
                        id: Long
                ) {
                    val option = parent.getItemAtPosition(position) as LedgerSpinnerOptions
                    if (option.key == 0) {
                        viewModel.currentStartMonth = viewModel.selectedMonth
                        viewModel.currentStartYear = viewModel.selectedYear
                        viewModel.currentEndMonth = viewModel.selectedMonth
                        viewModel.currentEndMonth = viewModel.selectedYear
                        viewModel.initiateLedgerData(viewModel.selectedMonth, viewModel.selectedYear, viewModel.selectedMonth, viewModel.selectedYear,false)
                    } else if (option.key == 1) {
                        viewModel.currentStartMonth = viewModel.selectedMonth
                        viewModel.currentStartYear = viewModel.selectedYear
                        viewModel.currentEndMonth = viewModel.selectedMonth
                        viewModel.currentEndMonth = viewModel.selectedYear
                        viewModel.initiateLedgerData(viewModel.selectedMonth, viewModel.selectedYear, viewModel.selectedMonth, viewModel.selectedYear,false)
                    } else if (option.key == 2) {
                        var month = viewModel.selectedMonth;
                        var year = viewModel.selectedYear;
                        if(month != 0){
                            month -= 1
                        }else{
                            month = 11
                            year -= 1
                        }
                        viewModel.currentStartMonth = month
                        viewModel.currentStartYear = year
                        viewModel.currentEndMonth = month
                        viewModel.currentEndMonth = year
                        viewModel.initiateLedgerData(month, year, month, year,false)
                    }else if(option.key == 3){
                        var startMonth = 0
                        var startYear = viewModel.selectedYear
                        var endMonth = viewModel.selectedMonth
                        var endYear = viewModel.selectedYear

                        if(viewModel.selectedMonth >= 2){
                            startMonth = endMonth - 2
                        }else{
                            startMonth = endMonth - 2 + 12
                            endYear -= 1
                        }
                        viewModel.currentStartMonth = startMonth
                        viewModel.currentStartYear = startYear
                        viewModel.currentEndMonth = endMonth
                        viewModel.currentEndMonth = endYear
                        viewModel.initiateLedgerData(startMonth, startYear, endMonth, endYear,false)
                    }else if(option.key == 4){
                        var startMonth = 0
                        var startYear = viewModel.selectedYear
                        var endMonth = viewModel.selectedMonth
                        var endYear = viewModel.selectedYear

                        if(viewModel.selectedMonth >= 5){
                            startMonth = endMonth - 5
                        }else{
                            startMonth = endMonth - 5 + 12
                            endYear -= 1
                        }
                        viewModel.currentStartMonth = startMonth
                        viewModel.currentStartYear = startYear
                        viewModel.currentEndMonth = endMonth
                        viewModel.currentEndMonth = endYear
                        viewModel.initiateLedgerData(startMonth, startYear, endMonth, endYear,false)
                    }
                    else if(option.key == 5) {

                        val df = SimpleDateFormat("yyyy-MM-dd")
                        val formatted: String = df.format(Date())
                        var endmonth = formatted.substring(6, 8)
                        var endYear = formatted.substring(0, 4).toInt()
                        var formattedEndMonth = endmonth.toInt()
                        var startMonth = 3
                        var startYear = endYear

                        if (endmonth.length == 2 && endmonth[0] == '0') {
                            formattedEndMonth = endmonth.substring(1).toInt()
                        }
                        if (formattedEndMonth <= 2) {
                            startYear = endYear - 1
                        }
                        viewModel.currentStartMonth = startMonth
                        viewModel.currentStartYear = startYear
                        viewModel.currentEndMonth = formattedEndMonth
                        viewModel.currentEndMonth = endYear
                        viewModel.initiateLedgerData(startMonth, startYear, formattedEndMonth, endYear,false)
                    }
                }
            }
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

//        viewModel.dataLoadingLiveData.observe(this, Observer {
//            isLoadingData = it ?: false
//        })

    }

    override fun onItemClicked(item: BaseConsolidatedPageRVAdapterItem<*>, position: Int) {
        TODO("Not yet implemented")
    }

    override fun onMonthClick(selectedMonth: Int) {
        uiUtils.showSnackbar("" + selectedMonth, Snackbar.LENGTH_LONG)
        viewModel.selectedMonth = selectedMonth

        var dialog = YearDialog()
        dialog.show(supportFragmentManager, "YearDialog")
    }

    override fun onYearClick(selectedYear: Int) {
        viewModel.selectedYear = selectedYear
        viewModel.initiateLedgerData(viewModel.selectedMonth, viewModel.selectedYear,viewModel.selectedMonth, viewModel.selectedYear,false)
    }

    override fun handleAction(actionId: String, position: Int, item: BaseConsolidatedPageRVAdapterItem<*>) {
        when (actionId){
            ConsolidatedMonthItemAction -> {
                val data = item.data as ConsolidatedMonthItemData
                adapter.toggle(position, data)
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
        viewModel.isLoadedNow = true
    }

    inner class PaginationInterface : PaginationScrollListener(UserTripsLoadLimit) {
        override fun loadMore() = viewModel.initiateLedgerData(viewModel.currentStartMonth,viewModel.currentStartYear,viewModel.currentEndMonth,viewModel.currentEndYear, true)

        override fun hasMore() = viewModel.hasMoreData

        //override fun isLoading() = isLoadingData
        override fun isLoading() = false
    }
}


private const val RandomKey = "RandomKey"

fun consolidatedPageIntent(
        context: Context
) = Intent(context, ConsolidatedPageActivity::class.java).apply {
    putExtra(RandomKey, "Hello")
}
