package com.delhivery.axle.ui.fastag

import android.Manifest
import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.BottomSheetStatementHistoryBinding
import com.delhivery.axle.databinding.DialogDownloadSuccessBinding
import com.delhivery.axle.databinding.FastagTransactionDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.profile.HelpSupportActivity
import com.delhivery.axle.utils.REQCODE_STORAGE
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FastagTransactionDetailsActivity : BaseActivity<FastagTransactionDetailsBinding, FastagTransactionDetailsViewModel>() {

    override fun getViewModelClass() = FastagTransactionDetailsViewModel::class.java
    override fun layoutId() = R.layout.fastag_transaction_details
    override fun requireConnection() = true

    private lateinit var adapter: FastagTransactionAdapter
    
    private var downloadID = 0L

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
        
        // Handle window insets for edge-to-edge display (API 35+)
        if (com.delhivery.axle.utils.WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            com.delhivery.axle.utils.WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
        }
        
        setupUI()
        setupRecyclerView()
        setupObservers()
        loadData()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }

    private fun setupUI() {
        // Get data from intent
        val vehicleNumber = intent.getStringExtra(VEHICLE_NUMBER) ?: ""
        val truckSize = intent.getStringExtra(TRUCK_SIZE) ?: ""
        val capacity = intent.getDoubleExtra(CAPACITY, 0.0)
        val ownership = intent.getStringExtra(OWNERSHIP) ?: ""
        val status = intent.getStringExtra(STATUS) ?: ""
        val balance = intent.getStringExtra(BALANCE) ?: "0"
        val issuedBy = intent.getStringExtra(ISSUED_BY) ?: ""
        val awb = intent.getStringExtra(AWB)

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
            binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
            binding.tvStatus.setBackgroundResource(com.delhivery.axle.R.drawable.bg_available_pill)
        } else {
            binding.tvStatus.visibility = android.view.View.GONE
        }
        

        
        // Handle AWB
        if (awb != null && awb.isNotEmpty()) {
            binding.layoutAwb.visibility = android.view.View.VISIBLE
            binding.tvAwb.text = awb
            
            // Copy AWB to clipboard
            binding.ivCopyAwb.setOnClickListener {
                val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("AWB", awb)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "AWB copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        } else {
            binding.layoutAwb.visibility = android.view.View.GONE
        }

        // Back button
        binding.ivBack.setOnClickListener {
            finish()
        }

        // Recharge button (placeholder)
        binding.btnRecharge.setOnClickListener {
            Toast.makeText(this, "Recharge functionality coming soon", Toast.LENGTH_SHORT).show()
        }

        // Download statement button - Show bottom sheet
        binding.btnDownload.setOnClickListener {
            showStatementHistoryBottomSheet()
        }
        
        // Account button - Call helpline
        binding.ivAccount.setOnClickListener {
            callHelpline()
        }
        
        // Help button - Navigate to Help & Support
        binding.ivHelp.setOnClickListener {
            navigationUtils.navigate(HelpSupportActivity::class.java)
        }
    }

    private fun setupRecyclerView() {
        adapter = FastagTransactionAdapter()
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.transactionsData.observe(this, androidx.lifecycle.Observer { response ->
            response?.transactions?.let {
                adapter.submitList(it)
            }
        })

        viewModel.errorData.observe(this, androidx.lifecycle.Observer { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.progressData.observe(this, androidx.lifecycle.Observer { isLoading ->
            // Show/hide progress indicator if needed
            if (isLoading) {
                uiUtils.showProgress()
            } else {
                uiUtils.hideProgress()
            }
        })
        
        // Observe download data
        viewModel.downloadData.observe(this, androidx.lifecycle.Observer { responseBody ->
            responseBody?.let {
                try {
                    // Save file to device
                    val filename = "FASTag_Statement_${System.currentTimeMillis()}.pdf"
                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    storageDir?.mkdirs()
                    val file = File(storageDir, filename)
                    
                    // Write ResponseBody to file
                    it.byteStream().use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    // Show success dialog
                    showDownloadSuccessDialog()
                    
                } catch (e: Exception) {
                    Log.e("FastagDownload", "Error saving file", e)
                    Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun loadData() {
        val tagId = intent.getStringExtra(TAG_ID) ?: return
        viewModel.loadTransactions(tagId)
    }
    
    private fun showStatementHistoryBottomSheet() {
        val dialog = Dialog(this)
        val bindingDialog = BottomSheetStatementHistoryBinding.inflate(layoutInflater)
        
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        
        // List of all item bindings for easier iteration
        val items = listOf(
            bindingDialog.itemRecent,
            bindingDialog.itemLastWeek,
            bindingDialog.itemCurrentMonth,
            bindingDialog.itemLastMonth,
            bindingDialog.itemLast3Months,
            bindingDialog.itemLast6Months
        )
        
        // Map items to identifiers
        val itemIds = listOf(
            R.id.itemRecent,
            R.id.itemLastWeek,
            R.id.itemCurrentMonth,
            R.id.itemLastMonth,
            R.id.itemLast3Months,
            R.id.itemLast6Months
        )
        
        var selectedIndex = 0 // Default to Recent
        
        // Helper to update selection UI
        fun updateSelection(index: Int) {
            selectedIndex = index
            items.forEachIndexed { i, item ->
                item.rbSelection.isChecked = (i == index)
                
                // Bold title for selected
                if (i == index) {
                    item.tvTitle.setTypeface(null, android.graphics.Typeface.BOLD)
                } else {
                    item.tvTitle.setTypeface(null, android.graphics.Typeface.NORMAL)
                }
            }
        }
        
        // Setup each item
        items.forEachIndexed { index, item ->
            val id = itemIds[index]
            
            // Set Title
            item.tvTitle.text = when(id) {
                R.id.itemRecent -> "Last 24 hours"
                R.id.itemLastWeek -> "Last 1 week"
                R.id.itemCurrentMonth -> "Current month"
                R.id.itemLastMonth -> "Last 1 month"
                R.id.itemLast3Months -> "Last 3 months"
                R.id.itemLast6Months -> "Last 6 months"
                else -> ""
            }
            
            // Set Subtitle (Date Range)
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
            
            // Handle Click
            item.root.setOnClickListener {
                updateSelection(index)
            }
            
            // Forward click from inner RB just in case, though it's disabled in XML
            item.rbSelection.setOnClickListener {
                updateSelection(index)
            }
        }
        
        // Initialize default selection
        updateSelection(0)
        
        // Handle download button click
        bindingDialog.btnDownloadStatement.setOnClickListener {
            val tagId = intent.getStringExtra(TAG_ID) ?: return@setOnClickListener
            
            dialog.dismiss()
            
            // Calculate date range based on selection
            val selectedId = itemIds[selectedIndex]
            val dateRange = calculateDateRange(selectedId)
            
            // Call download API
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
     * Download statement file using DownloadManager
     */
    private fun downloadStatement(url: String) {
        val mgr = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        
        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(downloadUri)
        
        // Generate filename with current date
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val filename = "FASTag_Statement_${dateFormat.format(Date())}.xlsx"
        val path = "/Axle App/$filename"
        
        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_WIFI or
                    DownloadManager.Request.NETWORK_MOBILE
        )
            .setTitle("FASTag Statement Download")
            .setDescription("Downloading...")
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOCUMENTS,
                path
            )
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        
        downloadID = mgr.enqueue(request)
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
