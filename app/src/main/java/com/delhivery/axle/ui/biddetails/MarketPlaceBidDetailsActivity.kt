package com.delhivery.axle.ui.biddetails

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityMarketplaceBidDetailsBinding
import com.delhivery.axle.databinding.ViewBidStatusAwaitingBinding
import com.delhivery.axle.databinding.ViewBidStatusConfirmedMarketplaceBinding
import com.delhivery.axle.databinding.ViewBidStatusRejectedBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.data.bids.TransactionBidStatus
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.DialogBidRevisedSuccessBinding
import com.delhivery.axle.ui.home.activity.home.homeActivityIntent
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import java.text.DecimalFormat
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * MarketPlace Bid Details screen
 * Shows bid details for marketplace loads with options to place bid, contact shipper, and view guidelines
 */
class MarketPlaceBidDetailsActivity : BaseActivity<ActivityMarketplaceBidDetailsBinding, MarketPlaceBidDetailsViewModel>() {

    init {
        hasInlineProgress = true
    }

    @Inject
    lateinit var userPrefs: UserPrefs

    private var countDownTimer: CountDownTimer? = null
    private var bidId: String? = null
    private var sourceCity: String? = null
    private var destinationCity: String? = null
    private var isGuidelinesExpanded = true

    companion object {
        private const val EXTRA_BID_ID = "extra_bid_id"
        private const val EXTRA_SOURCE_CITY = "extra_source_city"
        private const val EXTRA_DESTINATION_CITY = "extra_destination_city"

        fun start(
            context: Context,
            bidId: String,
            sourceCity: String = "",
            destinationCity: String = ""
        ) {
            val intent = Intent(context, MarketPlaceBidDetailsActivity::class.java).apply {
                putExtra(EXTRA_BID_ID, bidId)
                putExtra(EXTRA_SOURCE_CITY, sourceCity)
                putExtra(EXTRA_DESTINATION_CITY, destinationCity)
            }
            context.startActivity(intent)
        }
    }

    override fun getViewModelClass() = MarketPlaceBidDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_marketplace_bid_details

    override fun requireConnection() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Extract intent data
        bidId = intent.getStringExtra(EXTRA_BID_ID)
        sourceCity = intent.getStringExtra(EXTRA_SOURCE_CITY)
        destinationCity = intent.getStringExtra(EXTRA_DESTINATION_CITY)

        setupViews()
        setupListeners()
        setupObservers()

        // Load bid details - closing time will be parsed from API response
        bidId?.let {
            viewModel.loadBidDetails(it)
        }
    }

    private fun setupViews() {
        binding.apply {
            viewModel = this@MarketPlaceBidDetailsActivity.viewModel
            activity = this@MarketPlaceBidDetailsActivity
            lifecycleOwner = this@MarketPlaceBidDetailsActivity
        }
    }

    private fun setupListeners() {
        binding.apply {
            // Back button
            btnBack.setOnClickListener {
                onBackPressed()
            }

            // Share button
            btnShare.setOnClickListener {
                shareBidDetails()
            }

            // Place Bid button
            btnPlaceBid.setOnClickListener {
                placeBid()
            }

            // Call button
            btnCall.setOnClickListener {
                makePhoneCall()
            }

            // Guidelines toggle
            layoutGuidelinesHeader.setOnClickListener {
                toggleGuidelines()
            }

            // Bid amount text watcher
            editBidAmount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    validateBidAmount(s?.toString())
                }
            })
        }
    }

    private fun setupObservers() {
        // Observe loading state
        viewModel.isLoadingLiveData.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.marketplaceBidDetailLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.textClosingTime.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.btnShare.visibility = if (isLoading) View.GONE else View.VISIBLE
        })

        // Observe transaction details
        viewModel.transactionLiveData.observe(this, Observer { transaction ->
            transaction?.let {
                updateUIWithTransactionDetails(it)
            }
        })

        // Observe bid status from transaction bids API
        viewModel.bidStatusLiveData.observe(this, Observer { bidStatus ->
            bidStatus?.let {
                updateStatusCardsFromBidStatus(it)
            }
        })

        // Observe user's existing bid
        viewModel.userBidLiveData.observe(this, Observer { existingBid ->
            updateUIForBidState(existingBid)
        })

        // Observe bid placement result
        viewModel.bidPlacementResultLiveData.observe(this, Observer { result ->
            result?.let {
                if (it.success) {
                    // Determine if it's a new bid or revision
                    val isRevision = it.message?.contains("revised", ignoreCase = true) == true
                    showBidSuccessDialog(isRevision)
                } else {
                    Toast.makeText(this, it.errorMessage ?: "Failed to place bid", Toast.LENGTH_SHORT).show()
                }
            }
        })

        // Observe call button active state
        viewModel.isCallButtonActiveLiveData.observe(this, Observer { isActive ->
            binding.btnCall.isEnabled = isActive
            binding.btnCall.alpha = if (isActive) 1.0f else 0.5f
            
            // Toggle icon visibility
            if (isActive) {
                binding.disabledCallIv.visibility = View.GONE
                binding.enabledCallIv.visibility = View.VISIBLE
                binding.callText.setTextColor(android.graphics.Color.parseColor("#121A31"))
            } else {
                binding.disabledCallIv.visibility = View.VISIBLE
                binding.enabledCallIv.visibility = View.GONE
                binding.callText.setTextColor(android.graphics.Color.parseColor("#8F9198"))
            }
        })

        // Observe call initiation success
        viewModel.callInitiationLiveData.observe(this, Observer { response ->
            response?.let {
                if (it.success && !it.data.isNullOrEmpty()) {
                    val bridgeNumber = it.data.firstOrNull()?.bridgeNumber
                    if (bridgeNumber != null) {
                        // Successfully got bridge number, now make the call
                        dialPhoneNumber(bridgeNumber)
                        Toast.makeText(this, "Connecting call...", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Unable to get bridge number", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        // Observe call initiation error
        viewModel.callInitiationErrorLiveData.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        })

        // Observe call loading state
        viewModel.isCallLoadingLiveData.observe(this, Observer { isLoading ->
            if (isLoading) {
                // Show progress bar and hide icons
                binding.callProgressBar.visibility = View.VISIBLE
                binding.disabledCallIv.visibility = View.GONE
                binding.enabledCallIv.visibility = View.GONE
                binding.btnCall.isEnabled = false
                binding.btnCall.alpha = 0.7f
            } else {
                // Hide progress bar
                binding.callProgressBar.visibility = View.GONE
                // Restore button state based on isCallButtonActiveLiveData
                val isActive = viewModel.isCallButtonActiveLiveData.value ?: false
                binding.btnCall.isEnabled = isActive
                binding.btnCall.alpha = if (isActive) 1.0f else 0.5f
                
                // Restore icon visibility
                if (isActive) {
                    binding.disabledCallIv.visibility = View.GONE
                    binding.enabledCallIv.visibility = View.VISIBLE
                    binding.callText.setTextColor(android.graphics.Color.parseColor("#121A31"))
                } else {
                    binding.disabledCallIv.visibility = View.VISIBLE
                    binding.enabledCallIv.visibility = View.GONE
                    binding.callText.setTextColor(android.graphics.Color.parseColor("#8F9198"))
                }
            }
        })

        // Observe bid placement loading state
        viewModel.isBidPlacementLoadingLiveData.observe(this, Observer { isLoading ->
            if (isLoading) {
                // Show progress bar and hide button text
                binding.progressBar.visibility = View.VISIBLE
                binding.loadingOverlayTransparent.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.loadingOverlayTransparent.visibility = View.GONE
            }
        })
    }

    private fun startCountdownTimer(closingTimeMillis: Long) {
        val currentTime = System.currentTimeMillis()
        val timeRemaining = closingTimeMillis - currentTime

        if (timeRemaining <= 0) {
            binding.textClosingTime.text = "Bidding Closed"
            binding.btnPlaceBid.isEnabled = false
            return
        }

        countDownTimer = object : CountDownTimer(timeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

                val timeText = when {
                    hours > 0 -> String.format("Closes in %d hrs %d min", hours, minutes)
                    minutes > 0 -> String.format("Closes in %d min %d sec", minutes, seconds)
                    else -> String.format("Closes in %d sec", seconds)
                }

                binding.textClosingTime.text = timeText
            }

            override fun onFinish() {
                binding.textClosingTime.text = "Bidding Closed"
                binding.btnPlaceBid.isEnabled = false
            }
        }.start()
    }

    private fun updateUIForBidState(existingBid: com.delhivery.axle.data.bids.TransactionBid?) {
        if (existingBid != null) {
            // User has an existing bid - show it and change button text
            binding.editBidAmount.setText(existingBid.bidAmount.toInt().toString())
            binding.btnPlaceBid.text = "Revise Bid"
            // Enable button since we have an amount
            binding.btnPlaceBid.isEnabled = true
            binding.btnPlaceBid.alpha = 1.0f
        } else {
            // No existing bid - keep default state
            binding.editBidAmount.setText("")
            binding.btnPlaceBid.text = "Place Bid"
            // Disable button initially until amount is entered
            binding.btnPlaceBid.isEnabled = false
            binding.btnPlaceBid.alpha = 0.5f
        }
    }

    private fun updateUIWithTransactionDetails(transaction: com.delhivery.axle.data.home.bids.HomeBidsRequestItemData) {
        // Update origin and destination
        binding.originCity.text = transaction.origin.capitalize()
        binding.destinationCity.text = transaction.destination.capitalize()
        
        // Update truck type
        binding.textTruckType.text = transaction.getMarketplaceTruckInfo()
        
        // Update offer price using target_price
        val formattedPrice = if (transaction.targetPrice != null && transaction.targetPrice!! > 0) {
            "₹${com.delhivery.axle.utils.StringUtils.formatAmount(transaction.targetPrice!!)}"
        } else {
            "₹0"
        }
        binding.textOfferPrice.text = formattedPrice
        
        // Update shipper/POC information
        val displayName = transaction.getContactDisplayName()
        if (displayName.isNotEmpty()) {
            binding.textShipperName.text = displayName
            binding.textAvatar.text = transaction.getShipperInitials()
        }
        
        val phoneNumber = transaction.getContactPhoneNumber()
        if (phoneNumber.isNotEmpty()) {
            binding.textShipperPhone.text = phoneNumber
        }
        
        // Update payment mode
        if (transaction.shouldShowMarketplacePaymentMode()) {
            val paymentText = if (transaction.shouldShowMarketplaceAdvancePercentage()) {
                "${transaction.getMarketplacePaymentModeDisplay()} ${transaction.getMarketplaceAdvancePaymentPercentage()}"
            } else {
                transaction.getMarketplacePaymentModeDisplay()
            }
            binding.paymentMethod.text = paymentText
        }
        
        // Parse and start countdown timer from bidding_end_time
        transaction.contractBiddingEndTime?.let { endTime ->
            try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("IST")  // Set timezone to IST to match server response
                val date = sdf.parse(endTime)
                date?.let {
                    startCountdownTimer(it.time)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun validateBidAmount(amount: String?) {
        val isValid = !amount.isNullOrEmpty() && amount.toIntOrNull() != null && amount.toInt() > 0
        binding.btnPlaceBid.isEnabled = isValid
        // Update visual appearance based on validity
        binding.btnPlaceBid.alpha = if (isValid) 1.0f else 0.5f
    }

    private fun placeBid() {
        val bidAmount = binding.editBidAmount.text?.toString()?.toIntOrNull()

        if (bidAmount == null || bidAmount <= 0) {
            Toast.makeText(this, "Please enter a valid bid amount", Toast.LENGTH_SHORT).show()
            return
        }

        bidId?.let {
            viewModel.placeBid(it, bidAmount)
        } ?: run {
            Toast.makeText(this, "Invalid bid ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makePhoneCall() {
        // Use call masking API to get bridge number
        val transactionId = viewModel.transactionId
        val userBidId = viewModel.userExistingBid?.key()
        
        if (transactionId.isEmpty()) {
            Toast.makeText(this, "Transaction ID not available", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (userBidId.isNullOrEmpty()) {
            Toast.makeText(this, "Please place a bid before making a call", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModel.initiateMarketplaceCall(transactionId, userBidId)
    }

    /**
     * Dial the provided phone number (typically a bridge number from call masking)
     */
    private fun dialPhoneNumber(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to make call", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareBidDetails() {
        // Get route information
        val origin = binding.originCity.text.toString()
        val destination = binding.destinationCity.text.toString()
        val route = "$origin → $destination"
        
        // Get vehicle type
        val vehicleType = binding.textTruckType.text.toString()
        
        // Get shipper price
        val shipperPrice = binding.textOfferPrice.text.toString()
        
        // Get closing time
        val closingTime = binding.textClosingTime.text.toString()
        
        // Construct deep link with bid ID
        val deepLink = getString(R.string.axle_app_link)
        
        val shareText = buildString {
            append("🚛 New Load Alert – Spot Marketplace\n\n")
            append("📍 Route: $route\n")
            append("🚚 Vehicle: $vehicleType\n")
            append("💰 Shipper Price: $shipperPrice (Negotiable)\n")
            append("⏰ $closingTime\n\n")
            append("👉 Tap to bid: $deepLink")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Bid Details"))
    }

    private fun toggleGuidelines() {
        isGuidelinesExpanded = !isGuidelinesExpanded
        binding.layoutGuidelinesContent.visibility = if (isGuidelinesExpanded) View.VISIBLE else View.GONE
        
        // Rotate icon
        val rotation = if (isGuidelinesExpanded) 0f else 180f
        binding.iconGuidelinesToggle.animate().rotation(rotation).setDuration(200).start()
    }

    /**
     * Update status cards based on bid status from transaction bids API
     * This ensures we use the latest bid status from the bids API, not transaction details API
     */
    private fun updateStatusCardsFromBidStatus(bidStatus: MarketplaceBidStatus) {
        // Hide all status cards initially
        binding.confirmedBidClMarketplace.root.visibility = View.GONE
        binding.rejectedBidCl.root.visibility = View.GONE
        binding.awaitingBidCl.root.visibility = View.GONE
        binding.bidCard.visibility = View.VISIBLE

        when (bidStatus) {
            is MarketplaceBidStatus.NoBid -> {
                // No bid placed yet - show bid card
                binding.bidCard.visibility = View.VISIBLE
            }
            
            is MarketplaceBidStatus.Active -> {
                // Bid is active and can be revised - show bid card
                binding.bidCard.visibility = View.VISIBLE
            }
            
            is MarketplaceBidStatus.AwaitingResult -> {
                // Bidding closed, awaiting result
                binding.awaitingBidCl.root.visibility = View.VISIBLE
                binding.bidCard.visibility = View.GONE
                
                // Set data binding variables
                binding.awaitingBidCl.title = "Awaiting Result"
                binding.awaitingBidCl.subTitle = "We'll notify you once the results are out"
                binding.awaitingBidCl.actionLabel = "Explore New Bids"
                binding.awaitingBidCl.executePendingBindings()
                
                // Set click listener for action button
                binding.awaitingBidCl.btnAction.setOnClickListener {
                    startActivity(homeActivityIntent("load", this@MarketPlaceBidDetailsActivity))
                }
            }
            
            is MarketplaceBidStatus.Confirmed -> {
                // User's bid was confirmed/accepted
                binding.confirmedBidClMarketplace.root.visibility = View.VISIBLE
                binding.bidCard.visibility = View.GONE
                
                // Set data binding variables
                val bidAmount = bidStatus.bidAmount.toInt()
                binding.confirmedBidClMarketplace.title = "Bid Confirmed for ₹${DecimalFormat("#########").format(bidAmount)}"
                binding.confirmedBidClMarketplace.subTitle = "Provide the driver and vehicle details"
                binding.confirmedBidClMarketplace.actionLabel = "Call Shipper"
                binding.confirmedBidClMarketplace.executePendingBindings()
                
                // Set click listener for action button
                binding.confirmedBidClMarketplace.callShipper.setOnClickListener {
                    makePhoneCall()
                }
            }
            
            is MarketplaceBidStatus.Rejected -> {
                // User's bid was rejected/lost
                binding.rejectedBidCl.root.visibility = View.VISIBLE
                binding.bidCard.visibility = View.GONE
                
                // Set data binding variables
                binding.rejectedBidCl.title = "Bid not selected"
                
                // Try to get the winning bid amount if available
                binding.rejectedBidCl.subTitle = if (bidStatus.winningBidAmount != null && bidStatus.winningBidAmount > 0) {
                    "Winning bid price is ₹${DecimalFormat("#########").format(bidStatus.winningBidAmount.toInt())}"
                } else {
                    "Your bid was not selected for this load"
                }
                
                binding.rejectedBidCl.actionLabel = "Explore New Bids"
                binding.rejectedBidCl.executePendingBindings()
                
                // Set click listener for action button
                binding.rejectedBidCl.btnAction.setOnClickListener {
                    startActivity(homeActivityIntent("load", this@MarketPlaceBidDetailsActivity))
                }
            }
            
            is MarketplaceBidStatus.Cancelled -> {
                // Load was cancelled
                binding.rejectedBidCl.root.visibility = View.VISIBLE
                binding.bidCard.visibility = View.GONE
                
                // Set data binding variables
                binding.rejectedBidCl.title = "Demand cancelled"
                binding.rejectedBidCl.subTitle = "The demand was cancelled by the client"
                binding.rejectedBidCl.actionLabel = "Explore New Bids"
                binding.rejectedBidCl.executePendingBindings()
                
                // Set click listener for action button
                binding.rejectedBidCl.btnAction.setOnClickListener {
                    startActivity(homeActivityIntent("load", this@MarketPlaceBidDetailsActivity))
                }
            }
        }
    }

    /**
     * Show success bottom sheet dialog after bid placement/revision
     */
    private fun showBidSuccessDialog(isRevision: Boolean) {
        val dialog = Dialog(this)
        val bindingDialog = DialogBidRevisedSuccessBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        // Update dialog text based on whether it's a revision or new bid
        if (isRevision) {
            bindingDialog.textTitle.text = "Bid revised successfully!"
            bindingDialog.textMessage.text = "Your bid is now the lowest and you're in the best position to win."
        } else {
            bindingDialog.textTitle.text = "Bid placed successfully!"
            bindingDialog.textMessage.text = "Your bid has been submitted. We'll notify you once the results are out."
        }

        // Close button listener
        bindingDialog.btnClose.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}

