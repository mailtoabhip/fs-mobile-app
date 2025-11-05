package com.delhivery.axle.ui.biddetails

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityMarketplaceBidDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import java.text.DecimalFormat
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

            // WhatsApp button
            btnWhatsapp.setOnClickListener {
                openWhatsApp()
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
        })

        // Observe transaction details
        viewModel.transactionLiveData.observe(this, Observer { transaction ->
            transaction?.let {
                updateUIWithTransactionDetails(it)
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
                    val message = it.message ?: "Bid placed successfully!"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, it.errorMessage ?: "Failed to place bid", Toast.LENGTH_SHORT).show()
                }
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
            
            // Show a hint that this is a revision
            Toast.makeText(this, "You have an existing bid of ₹${existingBid.bidAmount.toInt()}", Toast.LENGTH_SHORT).show()
        } else {
            // No existing bid - keep default state
            binding.editBidAmount.setText("")
            binding.btnPlaceBid.text = "Place Bid"
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
        val phoneNumber = binding.textShipperPhone.text.toString()
        if (phoneNumber.isNotEmpty()) {
            try {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to make call", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openWhatsApp() {
        val phoneNumber = binding.textShipperPhone.text.toString()
        if (phoneNumber.isNotEmpty()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/$phoneNumber")
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareBidDetails() {
        val shareText = buildString {
            append("Bid Details\n\n")
            //append("Route: ${binding.textRoute.text}\n")
            append("Offer Price: ${binding.textOfferPrice.text}\n")
            append("Truck Type: ${binding.textTruckType.text}\n")
            //append("Payment: ${binding.textPayment.text}\n")
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

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}

