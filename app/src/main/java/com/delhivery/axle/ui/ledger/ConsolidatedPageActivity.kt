package com.delhivery.axle.ui.ledger

import android.Manifest
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.UserSearchLimitConsolidatedAPI
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemAction
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemData
import com.delhivery.axle.data.ledger.LedgerSpinnerOptions
import com.delhivery.axle.databinding.ActivityConsolidatedPageBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.DownloadLedgerDialog
import com.delhivery.axle.ui.dialogs.MonthDialog
import com.delhivery.axle.ui.dialogs.YearDialog
import com.delhivery.axle.utils.PaginationScrollListener
import com.delhivery.axle.utils.REQCODE_STORAGE
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import java.io.File
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

    var isLoadingData = true

    var downloadID = 0.toLong()

    var filePath = Uri.parse("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Your Money"

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            refreshData()
        }

        binding.rvConsolidatedLedger.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@ConsolidatedPageActivity.adapter
            addOnScrollListener(PaginationInterface())
        }

        binding.textRequestStatement.setOnClickListener{
            val dialog = DownloadLedgerDialog(this, viewModel)
            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            dialog.setOwnerActivity(this)
            if (!this.isFinishing)
                dialog.show()
            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
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
                        //viewModel.initiateLedgerData(viewModel.selectedMonth, viewModel.selectedYear, viewModel.selectedMonth, viewModel.selectedYear,false)
                    } else if (option.key == 1) {
                        viewModel.currentStartMonth = viewModel.selectedMonth
                        viewModel.currentStartYear = viewModel.selectedYear
                        viewModel.currentEndMonth = viewModel.selectedMonth
                        viewModel.currentEndMonth = viewModel.selectedYear
                        viewModel.initiateLedgerData(viewModel.selectedMonth, viewModel.selectedYear, viewModel.selectedMonth, viewModel.selectedYear, false)
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
                        viewModel.initiateLedgerData(month, year, month, year, false)
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
                        viewModel.initiateLedgerData(startMonth, startYear, endMonth, endYear, false)
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
                        viewModel.initiateLedgerData(startMonth, startYear, endMonth, endYear, false)
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
                        viewModel.initiateLedgerData(startMonth, startYear, formattedEndMonth, endYear, false)
                    }
                }
            }
        }

        viewModel.ledgerLiveData.observe(this, androidx.lifecycle.Observer {
            binding.refreshLayout.isRefreshing = false
            it?.let { _items ->
                adapter.operation(_items)
            }
        })

        viewModel.dataLoadingLiveData.observe(this, androidx.lifecycle.Observer {
            isLoadingData = it ?: false
        })

        viewModel.emailLoadingLiveData.observe(this, androidx.lifecycle.Observer {
            uiUtils.showSnackbar("" + it, Snackbar.LENGTH_LONG)
        })

        viewModel.downloadLoadingLiveData.observe(this, androidx.lifecycle.Observer {
            downloadLedger()
        })

        viewModel.downloadPressed.observe(this, androidx.lifecycle.Observer {
            if (it) {
                requestStoragePermission()
            }
        })

        openPopups()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }
    fun openPopups(){
        if(viewModel.isLoadedNow){
            var dialog = MonthDialog()
            dialog.show(supportFragmentManager, "MonthDialog")
            viewModel.isLoadedNow = false

        }
    }

    override fun onItemClicked(item: BaseConsolidatedPageRVAdapterItem<*>, position: Int) {
        return
    }

    override fun onMonthClick(selectedMonth: Int) {
        viewModel.selectedMonth = selectedMonth
        var dialog = YearDialog()
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "YearDialog")
    }

    override fun onYearClick(selectedYear: Int) {
        viewModel.selectedYear = selectedYear
        viewModel.initiateLedgerData(viewModel.selectedMonth, viewModel.selectedYear, viewModel.selectedMonth, viewModel.selectedYear, false)
    }

    override fun handleAction(actionId: String, position: Int, item: BaseConsolidatedPageRVAdapterItem<*>) {
        when (actionId){

            ConsolidatedLedgerItemAction -> {
                val data = item.data as ConsolidatedLedgerItemData
                adapter.toggle(position, data)
            }
        }
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        return true
    }

    private fun refreshData(){
        binding.refreshLayout.isRefreshing = true
        adapter.resetStaticData()
        viewModel.initiateLedgerData(viewModel.currentStartMonth, viewModel.currentStartYear, viewModel.currentEndMonth, viewModel.currentEndYear, false)
    }

    inner class PaginationInterface : PaginationScrollListener(UserSearchLimitConsolidatedAPI) {
        override fun loadMore() = viewModel.initiateLedgerData(viewModel.currentStartMonth, viewModel.currentStartYear, viewModel.currentEndMonth, viewModel.currentEndYear, true)

        override fun hasMore() = viewModel.hasMoreData

        override fun isLoading() = isLoadingData
    }

    private fun downloadLedger() {

//        val direct = File(getExternalFilesDir(null), "/Ledger")
//
//        if (!direct.exists()) {
//            direct.mkdirs()
//        }

        val mgr = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadUri = Uri.parse("https://51l1p3gsd7.execute-api.ap-southeast-1.amazonaws.com/prod/oracle-mis/applied/applied_2019-12-05_1575540958332.xlsx")
        val request = DownloadManager.Request(
                downloadUri
        )

        // TODO: add file name as month for which download requested
        val filename = "Ledger.xlsx"
        val path = "/Axle App/$filename"
        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or
                        DownloadManager.Request.NETWORK_MOBILE
        )
                .setTitle("Ledger Download")
                .setDescription("Downloading...")
                .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOCUMENTS,
                        path
                )
                .setNotificationVisibility(View.VISIBLE)

        downloadID = mgr.enqueue(request)
        filePath = Uri.parse(Environment.DIRECTORY_DOCUMENTS + path)
    }

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
                context: Context,
                intent: Intent
        ) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID === id) {
                uiUtils.showToast("File downloaded")
                openExcel()
            }
        }
    }

    private fun openExcel(){
        val newintent = Intent(Intent.ACTION_VIEW)
        newintent.setDataAndType(filePath, "application/vnd.ms-excel")
        newintent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        newintent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        try {
            startActivity(newintent)
        } catch (e: ActivityNotFoundException) {
            uiUtils.showToast("No Application Available to View Excel")
        }
    }

    private fun requestStoragePermission() {
        val storagePermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQCODE_STORAGE
            )
        } else {
            uiUtils.showSnackbar("Downloading File...")
        }
    }
}


private const val RandomKey = "RandomKey"

fun consolidatedPageIntent(
        context: Context
) = Intent(context, ConsolidatedPageActivity::class.java).apply {
    putExtra(RandomKey, "Hello")
}
