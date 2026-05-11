package com.delhivery.axle.ui.fastag

import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.BottomSheetStatementHistoryBinding
import com.delhivery.axle.databinding.DialogDownloadSuccessBinding
import com.delhivery.axle.databinding.FastagTransactionDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.fastag.FastagDynamicDisputeFormActivity.Companion.EXTRA_TXN_ID
import com.delhivery.axle.ui.profile.HelpSupportActivity
import com.delhivery.axle.utils.EVENT_FASTAG_STATEMENT_DOWNLOADED
import com.delhivery.axle.utils.EVENT_FASTAG_TXN_LIST_SHOWN
import com.delhivery.axle.utils.PROPERTY_PAGE_NAME
import com.delhivery.axle.utils.PROPERTY_RANGE
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.REQCODE_STORAGE
import com.delhivery.axle.utils.VALUE_FASTAG_DISPUTE_PAGE
import com.delhivery.axle.utils.VALUE_FASTAG_TXN_LIST_PAGE
import com.delhivery.axle.utils.extensions.getSerializable
import com.delhivery.axle.utils.prefs.UserPrefs
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class FastagTransactionDetailsActivity : BaseActivity<FastagTransactionDetailsBinding, FastagTransactionDetailsViewModel>() {

    override fun getViewModelClass() = FastagTransactionDetailsViewModel::class.java
    override fun layoutId() = R.layout.fastag_transaction_details
    override fun requireConnection() = true

    private lateinit var adapter: FastagTransactionAdapter
    
    private var downloadID = 0L

    @Inject lateinit var userPrefs: UserPrefs

    private var range: String = ""

    companion object {
        const val TAG_ID = "tag_id"
        const val VRN = "vrn"
        const val VEHICLE_NUMBER = "vehicle_number"
        const val TRUCK_TYPE = "truck_type"
        const val TRUCK_SIZE = "truck_size"
        const val CAPACITY = "capacity"
        const val OWNERSHIP = "ownership"
        const val STATUS = "status"
        const val TAG_STATUS = "tag_status"
        const val BALANCE = "balance"
        const val ISSUED_BY = "issued_by"
        const val AWB = "awb"
        const val VEHICLE_DATA = "vehicle_data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Register broadcast receiver for download completion
        ContextCompat.registerReceiver(
            this,
            onDownloadComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        analyticsUtil.moEngageTrackEvent(
            EVENT_FASTAG_TXN_LIST_SHOWN,
            mutableListOf(
                PROPERTY_USER_ID,
                PROPERTY_PAGE_NAME
            ),
            mutableListOf(
                userPrefs.userId(),
                VALUE_FASTAG_TXN_LIST_PAGE
            )
        )

        setupUI()
        setupRecyclerView()
        setupObservers()
        loadData()


    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        /* Handle window insets for edge-to-edge display (API 35+) */
        if (com.delhivery.axle.utils.WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            com.delhivery.axle.utils.WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
        }
    }

    private var isFirstResume = true

    override fun onResume() {
        super.onResume()
        if (isFirstResume) {
            isFirstResume = false
            return
        }
        loadData()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }

    private fun setupUI() {
        val vehicleData = intent.getSerializable(VEHICLE_DATA, com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData::class.java)
        val vehicleNumber = vehicleData?.vehicleNumber ?: intent.getStringExtra(VEHICLE_NUMBER) ?: ""
        val truckSize = vehicleData?.truckSize ?: intent.getStringExtra(TRUCK_SIZE) ?: ""
        val capacity = vehicleData?.capacity ?: intent.getDoubleExtra(CAPACITY, 0.0)
        val ownership = vehicleData?.ownership ?: intent.getStringExtra(OWNERSHIP) ?: ""
        val status = vehicleData?.latestStatus ?: intent.getStringExtra(STATUS) ?: ""
        val balance = vehicleData?.fastagBalance ?: intent.getStringExtra(BALANCE) ?: "0"
        val issuedBy = vehicleData?.fastagIssuedBy ?: intent.getStringExtra(ISSUED_BY) ?: ""
        val awb = vehicleData?.fastagTagId ?: intent.getStringExtra(AWB)

        // Set vehicle info
        binding.tvVehicleNumber.text = vehicleNumber
        
        // Map ownership value
        val ownershipDisplay = when(ownership) {
            "owns_truck" -> "Own Truck"
            "market_truck" -> "Market Truck"
            else -> ownership
        }
        
        binding.tvVehicleMeta.text = "$ownershipDisplay | $truckSize | $capacity MT"
        binding.tvBalance.text = "₹$balance balance available"
        binding.tvFastagProvider.text = "$issuedBy FASTag by Delhivery"
        
        // Set status badge - show "Available" only if truck's latest_status is "Free"
        if (status.equals("Free", ignoreCase = true)) {
            binding.tvStatus.text = "Available"
            binding.tvStatus.visibility = android.view.View.VISIBLE
            binding.tvStatus.setBackgroundResource(R.drawable.bg_available_pill)
        } else {
            binding.tvStatus.visibility = android.view.View.GONE
        }

        if (awb != null && awb.isNotEmpty()) {
            binding.layoutAwb.visibility = android.view.View.VISIBLE
            
            // Create spannable string with "ID: " in gray and awb in red
            val fullText = "ID: $awb"
            val spannableString = SpannableString(fullText)
            
            // Set "ID: " to gray color
            spannableString.setSpan(
                ForegroundColorSpan(Color.parseColor("#525B7A")),
                0,
                3, // Length of "ID: "
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            
            // Set awb to red color
            spannableString.setSpan(
                ForegroundColorSpan(Color.parseColor("#FA3A2E")),
                3,
                fullText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            
            binding.tvAwb.text = spannableString
            
            binding.ivCopyAwb.setOnClickListener {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("AWB", awb)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Fastag Id copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        } else {
            binding.layoutAwb.visibility = android.view.View.GONE
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnRecharge.setOnClickListener {
            val intent = android.content.Intent(this, FastagRechargeActivity::class.java).apply {
                putExtra(FastagRechargeActivity.TAG_ID, vehicleData?.fastagTagId)
                putExtra(FastagRechargeActivity.VEHICLE_NUMBER, vehicleData?.vehicleNumber)
                putExtra(FastagRechargeActivity.FASTAG_BALANCE, vehicleData?.fastagBalance)
            }
            startActivity(intent)
        }

        binding.btnDownload.setOnClickListener {
            showStatementHistoryBottomSheet()
        }
        
        binding.ivAccount.setOnClickListener {
            callHelpline()
        }
        
        binding.ivHelp.setOnClickListener {
            navigationUtils.navigate(HelpSupportActivity::class.java)
        }
    }

    private fun setupRecyclerView() {
        val vehicleData = intent.getSerializable(VEHICLE_DATA, com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData::class.java)
        adapter = FastagTransactionAdapter(vehicleData)
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter

        // RecyclerView is inside a NestedScrollView with nestedScrollingEnabled=false,
        // so RecyclerView scroll events won't fire. Listen to NestedScrollView scroll instead.
        val nestedScrollView = binding.rvTransactions.parent?.parent as? androidx.core.widget.NestedScrollView
        nestedScrollView?.setOnScrollChangeListener(
            androidx.core.widget.NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
                val contentHeight = v.getChildAt(0).measuredHeight
                val scrollViewHeight = v.measuredHeight
                val distanceFromBottom = contentHeight - scrollViewHeight - scrollY

                if (distanceFromBottom < 300 && viewModel.hasNext) {
                    val vehicleDataInner = intent.getSerializable(VEHICLE_DATA, com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData::class.java)
                    val tagId = vehicleDataInner?.fastagTagId ?: intent.getStringExtra(TAG_ID) ?: return@OnScrollChangeListener
                    viewModel.loadTransactions(tagId, loadMore = true)
                }
            }
        )
    }

    private fun setupObservers() {
        viewModel.transactionsData.observe(this, androidx.lifecycle.Observer { response ->
            response?.transactions?.let {
                adapter.submitList(it)

                binding.btnDownload.isEnabled = it.isNotEmpty()
                binding.btnDownload.alpha = if (it.isNotEmpty()) 1.0f else 0.5f
            }
        })

        viewModel.errorData.observe(this, androidx.lifecycle.Observer { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.progressData.observe(this, androidx.lifecycle.Observer { isLoading ->
            if (isLoading) {
                uiUtils.showProgress()
            } else {
                uiUtils.hideProgress()
            }
        })
        
        viewModel.downloadData.observe(this, androidx.lifecycle.Observer { responseBody ->
            responseBody?.let {
                try {
                    // Save file to device
                    val filename = "FASTag_Statement_${System.currentTimeMillis()}.pdf"
                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    storageDir?.mkdirs()
                    val file = File(storageDir, filename)
                    
                    it.byteStream().use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    sendSuccessfulDownloadEvent()
                    showDownloadSuccessDialog()
                    
                } catch (e: Exception) {
                    Log.e("FastagDownload", "Error saving file", e)
                    Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun loadData() {
        val vehicleData = intent.getSerializable(VEHICLE_DATA, com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData::class.java)
        val tagId = vehicleData?.fastagTagId ?: intent.getStringExtra(TAG_ID) ?: return
        viewModel.loadTransactions(tagId)
    }
    
    private fun showStatementHistoryBottomSheet() {
        val dialog = Dialog(this)
        val bindingDialog = BottomSheetStatementHistoryBinding.inflate(layoutInflater)
        
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        
        val items = listOf(
            bindingDialog.itemRecent,
            bindingDialog.itemLastWeek,
            bindingDialog.itemCurrentMonth,
            bindingDialog.itemLastMonth,
            bindingDialog.itemLast3Months,
            bindingDialog.itemLast6Months
        )
        
        val itemIds = listOf(
            R.id.itemRecent,
            R.id.itemLastWeek,
            R.id.itemCurrentMonth,
            R.id.itemLastMonth,
            R.id.itemLast3Months,
            R.id.itemLast6Months
        )
        
        var selectedIndex = 0
        
        fun updateSelection(index: Int) {
            selectedIndex = index
            items.forEachIndexed { i, item ->
                item.rbSelection.isChecked = (i == index)
                
                if (i == index) {
                    item.tvTitle.setTypeface(null, android.graphics.Typeface.BOLD)
                } else {
                    item.tvTitle.setTypeface(null, android.graphics.Typeface.NORMAL)
                }
            }
        }
        
        items.forEachIndexed { index, item ->
            val id = itemIds[index]
            
            item.tvTitle.text = when(id) {
                R.id.itemRecent -> "Last 24 hours"
                R.id.itemLastWeek -> "Last 1 week"
                R.id.itemCurrentMonth -> "Current month"
                R.id.itemLastMonth -> "Last 1 month"
                R.id.itemLast3Months -> "Last 3 months"
                R.id.itemLast6Months -> "Last 6 months"
                else -> ""
            }
            
            val dateRange = calculateDateRange(id)
            val formattedRange = formatDateRangeForDisplay(dateRange.first, dateRange.second, id)
            
            if (formattedRange.isNotEmpty()) {
                item.tvSubtitle.text = formattedRange
                item.tvSubtitle.visibility = android.view.View.VISIBLE
            } else {
                item.tvSubtitle.visibility = android.view.View.GONE
            }

            if (id == R.id.itemCurrentMonth) {
                val cal = Calendar.getInstance()
                item.tvSubtitle.text = SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
                item.tvSubtitle.visibility = android.view.View.VISIBLE
            }
            
            item.root.setOnClickListener {
                updateSelection(index)
            }
            
            item.rbSelection.setOnClickListener {
                updateSelection(index)
            }
        }
        
        updateSelection(0)
        
        bindingDialog.btnDownloadStatement.setOnClickListener {
            val vehicleData = intent.getSerializable(VEHICLE_DATA, com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData::class.java)
            val tagId = vehicleData?.fastagTagId ?: intent.getStringExtra(TAG_ID) ?: return@setOnClickListener
            
            dialog.dismiss()
            
            val selectedId = itemIds[selectedIndex]
            val dateRange = calculateDateRange(selectedId)

            range = dateRange.first + dateRange.second
            
            viewModel.downloadTransactions(tagId, dateRange.first, dateRange.second)
        }
        
        dialog.show()
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes.windowAnimations = R.style.DialogAnimation
            setGravity(Gravity.BOTTOM)
        }
    }


    /**
     * Formats date range like "21st Dec 2025 to 21st Jan 2026"
     */
    private fun formatDateRangeForDisplay(fromDateStr: String, toDateStr: String, id: Int): String {
        try {
            // Input format is now "dd-MM-yyyy HH:mm:ss"
            val inputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val fromDate = inputFormat.parse(fromDateStr) ?: return ""
            val toDate = inputFormat.parse(toDateStr) ?: return ""
            
            val dayFormat = SimpleDateFormat("d", Locale.getDefault())
            val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            
            fun getDayWithSuffix(date: Date): String {
                val day = dayFormat.format(date).toInt()
                val suffix = when {
                    day in 11..13 -> "th"
                    day % 10 == 1 -> "st"
                    day % 10 == 2 -> "nd"
                    day % 10 == 3 -> "rd"
                    else -> "th"
                }
                return "$day$suffix"
            }
            
            val fromDay = getDayWithSuffix(fromDate)
            val fromMonth = monthFormat.format(fromDate)
            val fromYear = yearFormat.format(fromDate)
            val toDay = getDayWithSuffix(toDate)
            val toMonth = monthFormat.format(toDate)
            val toYear = yearFormat.format(toDate)
            
            // For Current Month, we only show month name (e.g. "January")
            if (id == R.id.itemCurrentMonth) {
                return monthFormat.format(toDate)
            }
            
            // Per request: No dates for Recent transactions and Last 1 week
            if (id == R.id.itemRecent || id == R.id.itemLastWeek) return "" 
            
            // Show year only if years are different
            return if (fromYear == toYear) {
                "$fromDay $fromMonth to $toDay $toMonth"
            } else {
                "$fromDay $fromMonth $fromYear to $toDay $toMonth $toYear"
            }
            
        } catch (e: Exception) {
            Log.e("FastagTransactions", "Error formatting date range", e)
            return ""
        }
    }
    /**
     * Broadcast receiver for download completion
     */
    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                // Run on main thread and check if activity is active
                runOnUiThread {
                    if (!isFinishing && !isDestroyed) {
                        showDownloadSuccessDialog()
                    }
                }
            }
        }
    }

    private fun sendSuccessfulDownloadEvent() {
        analyticsUtil.moEngageTrackEvent(
            EVENT_FASTAG_STATEMENT_DOWNLOADED,
            mutableListOf(
                PROPERTY_USER_ID,
                PROPERTY_PAGE_NAME,
                PROPERTY_RANGE
            ),
            mutableListOf(
                userPrefs.userId(),
                VALUE_FASTAG_TXN_LIST_PAGE,
                range
            )
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQCODE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, download will be triggered from observer
                Toast.makeText(this, "Permission granted. Please try downloading again.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Storage permission is required to download files", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Show success dialog when download completes
     */
    private fun showDownloadSuccessDialog() {
        try {
            val dialog = Dialog(this)
            val bindingDialog = DialogDownloadSuccessBinding.inflate(layoutInflater)
            
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(bindingDialog.root)
            
            // Set window properties before showing
            dialog.window?.apply {
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                attributes.windowAnimations = R.style.DialogAnimation
                setGravity(Gravity.BOTTOM)
            }
            
            // Close button
            bindingDialog.ivClose.setOnClickListener {
                dialog.dismiss()
            }
            
            // Done button
            bindingDialog.btnDone.setOnClickListener {
                dialog.dismiss()
            }
            
            dialog.show()
        } catch (e: Exception) {
            android.util.Log.e("FastagDownload", "Error showing dialog", e)
        }
    }

    /**
     * Calculate date range based on radio button selection
     * Returns Pair of (fromDate, toDate) in format "dd-MM-yyyy HH:mm:ss"
     */
    private fun calculateDateRange(selectedId: Int): Pair<String, String> {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val toDate: String
        val fromDate: String
        
        when (selectedId) {
            R.id.itemRecent -> {
                // To date: current time
                val toCalendar = Calendar.getInstance()
                toDate = dateFormat.format(toCalendar.time)
                
                // From date: 24 hours ago
                val fromCalendar = Calendar.getInstance()
                fromCalendar.add(Calendar.HOUR_OF_DAY, -24)
                fromDate = dateFormat.format(fromCalendar.time)
            }
            R.id.itemLastWeek -> {
                // To date: end of today
                val toCalendar = Calendar.getInstance()
                toCalendar.set(Calendar.HOUR_OF_DAY, 23)
                toCalendar.set(Calendar.MINUTE, 59)
                toCalendar.set(Calendar.SECOND, 59)
                toDate = dateFormat.format(toCalendar.time)
                
                // From date: start of 7 days ago
                val fromCalendar = Calendar.getInstance()
                fromCalendar.add(Calendar.DAY_OF_YEAR, -7)
                fromCalendar.set(Calendar.HOUR_OF_DAY, 0)
                fromCalendar.set(Calendar.MINUTE, 0)
                fromCalendar.set(Calendar.SECOND, 0)
                fromDate = dateFormat.format(fromCalendar.time)
            }
            R.id.itemCurrentMonth -> {
                // To date: end of today
                val toCalendar = Calendar.getInstance()
                toCalendar.set(Calendar.HOUR_OF_DAY, 23)
                toCalendar.set(Calendar.MINUTE, 59)
                toCalendar.set(Calendar.SECOND, 59)
                toDate = dateFormat.format(toCalendar.time)
                
                // From date: start of first day of current month
                val fromCalendar = Calendar.getInstance()
                fromCalendar.set(Calendar.DAY_OF_MONTH, 1)
                fromCalendar.set(Calendar.HOUR_OF_DAY, 0)
                fromCalendar.set(Calendar.MINUTE, 0)
                fromCalendar.set(Calendar.SECOND, 0)
                fromDate = dateFormat.format(fromCalendar.time)
            }
            R.id.itemLastMonth -> {
                // To date: end of today
                val toCalendar = Calendar.getInstance()
                toCalendar.set(Calendar.HOUR_OF_DAY, 23)
                toCalendar.set(Calendar.MINUTE, 59)
                toCalendar.set(Calendar.SECOND, 59)
                toDate = dateFormat.format(toCalendar.time)
                
                // From date: start of 1 month ago
                val fromCalendar = Calendar.getInstance()
                fromCalendar.add(Calendar.MONTH, -1)
                fromCalendar.set(Calendar.HOUR_OF_DAY, 0)
                fromCalendar.set(Calendar.MINUTE, 0)
                fromCalendar.set(Calendar.SECOND, 0)
                fromDate = dateFormat.format(fromCalendar.time)
            }
            R.id.itemLast3Months -> {
                // To date: end of today
                val toCalendar = Calendar.getInstance()
                toCalendar.set(Calendar.HOUR_OF_DAY, 23)
                toCalendar.set(Calendar.MINUTE, 59)
                toCalendar.set(Calendar.SECOND, 59)
                toDate = dateFormat.format(toCalendar.time)
                
                // From date: start of 3 months ago
                val fromCalendar = Calendar.getInstance()
                fromCalendar.add(Calendar.MONTH, -3)
                fromCalendar.set(Calendar.HOUR_OF_DAY, 0)
                fromCalendar.set(Calendar.MINUTE, 0)
                fromCalendar.set(Calendar.SECOND, 0)
                fromDate = dateFormat.format(fromCalendar.time)
            }
            R.id.itemLast6Months -> {
                // To date: end of today
                val toCalendar = Calendar.getInstance()
                toCalendar.set(Calendar.HOUR_OF_DAY, 23)
                toCalendar.set(Calendar.MINUTE, 59)
                toCalendar.set(Calendar.SECOND, 59)
                toDate = dateFormat.format(toCalendar.time)
                
                // From date: start of 6 months ago
                val fromCalendar = Calendar.getInstance()
                fromCalendar.add(Calendar.MONTH, -6)
                fromCalendar.set(Calendar.HOUR_OF_DAY, 0)
                fromCalendar.set(Calendar.MINUTE, 0)
                fromCalendar.set(Calendar.SECOND, 0)
                fromDate = dateFormat.format(fromCalendar.time)
            }
            else -> {
                // To date: end of today
                val toCalendar = Calendar.getInstance()
                toCalendar.set(Calendar.HOUR_OF_DAY, 23)
                toCalendar.set(Calendar.MINUTE, 59)
                toCalendar.set(Calendar.SECOND, 59)
                toDate = dateFormat.format(toCalendar.time)
                
                // From date: start of 15 days ago
                val fromCalendar = Calendar.getInstance()
                fromCalendar.add(Calendar.DAY_OF_YEAR, -15)
                fromCalendar.set(Calendar.HOUR_OF_DAY, 0)
                fromCalendar.set(Calendar.MINUTE, 0)
                fromCalendar.set(Calendar.SECOND, 0)
                fromDate = dateFormat.format(fromCalendar.time)
            }
        }
        
        return Pair(fromDate, toDate)
    }
}
