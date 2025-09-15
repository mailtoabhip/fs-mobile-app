package com.delhivery.axle.ui.ledger

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import com.delhivery.axle.data.home.loads.HomeLoadsTimeOutAction
import com.delhivery.axle.data.home.loads.HomeLoadsWarningAction_NoLoads
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemAction
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemData
import com.delhivery.axle.data.ledger.LedgerSpinnerOptions
import com.delhivery.axle.databinding.ActivityConsolidatedPageBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.DownloadLedgerDialog
import com.delhivery.axle.utils.PaginationScrollListener
import com.delhivery.axle.utils.REQCODE_STORAGE
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class ConsolidatedPageActivity: BaseActivity<ActivityConsolidatedPageBinding, ConsolidatedPageViewModel>(),
        ConsolidatedPageRVAdapterInterface,
        NavigationView.OnNavigationItemSelectedListener{
    override fun getViewModelClass() = ConsolidatedPageViewModel::class.java

    override fun layoutId() = R.layout.activity_consolidated_page

    override fun requireConnection() = true

    private val adapter: ConsolidatedPageRVAdapter by lazy { ConsolidatedPageRVAdapter(this) }
    private val ledgerSpinnerAdapter: LedgerSpinnerAdapter by lazy { LedgerSpinnerAdapter() }

    var isLoadingData = true

    var isRecent = true

    @Inject lateinit var userPrefs: UserPrefs

    var downloadID = 0.toLong()
    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("ConsolidatedPageActivity_SetupTime")
        activitySetupTrace?.start()
        ContextCompat.registerReceiver(this,onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "My Earnings"

        val downloadIntent = intent?.getBooleanExtra(RandomKey , false)
        if(downloadIntent == true){
            openLedgerDialog()
        }

        val df = SimpleDateFormat("yyyy-MM-dd")
        val formatted: String = df.format(Date())
        val endmonth = formatted.substring(5, 7)
        val endYear = formatted.substring(0, 4).toInt()
        val formattedEndMonth = endmonth.toInt() - 1

        viewModel.currentStartMonth = formattedEndMonth
        viewModel.currentStartYear = endYear
        viewModel.currentEndMonth = formattedEndMonth
        viewModel.currentEndYear = endYear

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            viewModel.offset = 0
            refreshData(viewModel.currentStartMonth, viewModel.currentStartYear, viewModel.currentEndMonth, viewModel.currentEndYear, isRecent)
        }

        binding.rvConsolidatedLedger.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@ConsolidatedPageActivity.adapter
            addOnScrollListener(PaginationInterface())
        }

        binding.textRequestStatement.setOnClickListener{
            openLedgerDialog()
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
                    when (option.key) {
                        0 -> {
                            isRecent = true
                            setSelection(formattedEndMonth, endYear, formattedEndMonth, endYear)
                            refreshData(formattedEndMonth, endYear, formattedEndMonth, endYear, isRecent)
                        }
                        1 -> {
                            isRecent = false
                            setSelection(formattedEndMonth, endYear, formattedEndMonth, endYear)
                            refreshData(formattedEndMonth, endYear, formattedEndMonth, endYear)
                        }
                        2 -> {
                            isRecent = false
                            var month = formattedEndMonth
                            var year = endYear
                            if(month != 0){
                                month -= 1
                            }else{
                                month = 11
                                year -= 1
                            }
                            setSelection(month, year, month, year)
                            refreshData(month, year, month, year)
                        }
                        3 -> {
                            isRecent = false
                            val startMonth: Int
                            var startYear = endYear

                            if(formattedEndMonth >= 2){
                                startMonth = formattedEndMonth - 2
                            }else{
                                startMonth = formattedEndMonth - 2 + 12
                                startYear -= 1
                            }
                            setSelection(startMonth, startYear, formattedEndMonth, endYear)
                            refreshData(startMonth, startYear, formattedEndMonth, endYear)
                        }
                        4 -> {
                            isRecent = false
                            val startMonth: Int
                            var startYear = endYear

                            if(formattedEndMonth >= 5){
                                startMonth = formattedEndMonth - 5
                            }else{
                                startMonth = formattedEndMonth - 5 + 12
                                startYear -= 1
                            }
                            setSelection(startMonth, startYear, formattedEndMonth, endYear)
                            refreshData(startMonth, startYear, formattedEndMonth, endYear)
                        }
                        5 -> {
                            isRecent = false
                            val startMonth = 3
                            var startYear = endYear

                            if (formattedEndMonth <= 2) {
                                startYear = endYear - 1
                            }
                            setSelection(startMonth, startYear, formattedEndMonth, endYear)
                            refreshData(startMonth, startYear, formattedEndMonth, endYear)
                        }
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
            downloadLedger(it.url)
        })

        viewModel.downloadPressed.observe(this, androidx.lifecycle.Observer {
            if (it && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                requestStoragePermission()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }

    private fun openLedgerDialog() {
        val dialog = DownloadLedgerDialog(this, viewModel, analyticsUtil, userPrefs)
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.setOwnerActivity(this)
        if (!this.isFinishing)
            dialog.show()
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val downloadIntent = intent?.getBooleanExtra(RandomKey , false)
        if(downloadIntent == true){
            openLedgerDialog()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }

    override fun onItemClicked(item: BaseConsolidatedPageRVAdapterItem<*>, position: Int) {
        return
    }

    override fun handleAction(actionId: String, position: Int, item: BaseConsolidatedPageRVAdapterItem<*>) {
        when (actionId){

            ConsolidatedLedgerItemAction -> {
                val data = item.data as ConsolidatedLedgerItemData
                adapter.toggle(position, data)
            }

            HomeLoadsTimeOutAction -> {
                refreshData(viewModel.currentStartMonth, viewModel.currentStartYear, viewModel.currentEndMonth, viewModel.currentEndYear, isRecent)
            }

            HomeLoadsWarningAction_NoLoads ->{
                refreshData(viewModel.currentStartMonth, viewModel.currentStartYear, viewModel.currentEndMonth, viewModel.currentEndYear, isRecent)
            }
        }
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        return true
    }

    private fun setSelection(startMonth: Int, startYear: Int, endMonth: Int, endYear: Int){
        viewModel.offset = 0
        viewModel.currentStartMonth = startMonth
        viewModel.currentStartYear = startYear
        viewModel.currentEndMonth = endMonth
        viewModel.currentEndYear = endYear
    }

    private fun refreshData(startMonth:Int, startYear:Int,endMonth:Int, endYear:Int, recent:Boolean = false){
        binding.refreshLayout.isRefreshing = true
        adapter.resetStaticData()
        if(recent){
            binding.spinnerShowing.setSelection(0)
            viewModel.initiateLedgerData(startMonth, startYear, endMonth, endYear, paginate = false, recent = true)
        }else{
            viewModel.initiateLedgerData(startMonth, startYear, endMonth, endYear, false)
        }
    }

    inner class PaginationInterface : PaginationScrollListener(UserSearchLimitConsolidatedAPI, isConsolidatedApi = true) {
        override fun loadMore() = viewModel.initiateLedgerData(viewModel.currentStartMonth, viewModel.currentStartYear, viewModel.currentEndMonth, viewModel.currentEndYear, true, isRecent)

        override fun hasMore() = viewModel.hasMoreData

        override fun isLoading() = isLoadingData
    }

    private fun downloadLedger(url: String) {

        val mgr = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(
                downloadUri
        )

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
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)


        downloadID = mgr.enqueue(request)
    }

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
                context: Context,
                intent: Intent
        ) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                uiUtils.showToast("File downloaded, please check notification.")
            }
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
            uiUtils.showToast("Downloading File...")
        }
    }
}


private const val RandomKey = "RandomKey"

fun consolidatedPageIntent(
        context: Context,
        downloadIntent: Boolean = false
) = Intent(context, ConsolidatedPageActivity::class.java).apply {
    putExtra(RandomKey, downloadIntent)
}
