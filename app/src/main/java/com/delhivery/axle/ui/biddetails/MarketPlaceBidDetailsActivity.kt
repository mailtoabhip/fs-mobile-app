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
        private const val EXTRA_CLOSING_TIME = "extra_closing_time"

        fun start(
            context: Context,
            bidId: String,
            sourceCity: String,
            destinationCity: String,
            closingTime: Long
        ) {
            val intent = Intent(context, MarketPlaceBidDetailsActivity::class.java).apply {
                putExtra(EXTRA_BID_ID, bidId)
                putExtra(EXTRA_SOURCE_CITY, sourceCity)
                putExtra(EXTRA_DESTINATION_CITY, destinationCity)
                putExtra(EXTRA_CLOSING_TIME, closingTime)
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
        val closingTime = intent.getLongExtra(EXTRA_CLOSING_TIME, 0L)

        setupViews()
        setupListeners()
        setupObservers()

        // Set route text
        if (sourceCity.isNotNullOrEmpty() && destinationCity.isNotNullOrEmpty()) {
            //binding.textRoute.text = "$sourceCity → $destinationCity"
        }

        // Start countdown timer
        if (closingTime > 0) {
            startCountdownTimer(closingTime)
        }

        // Load bid details
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
        // Observe bid details
        viewModel.bidDetailsLiveData.observe(this, Observer { bidDetails ->
            bidDetails?.let {
                updateUIWithBidDetails(it)
            }
        })

        // Observe bid placement result
        viewModel.bidPlacementResultLiveData.observe(this, Observer { result ->
            result?.let {
                if (it.success) {
                    Toast.makeText(this, "Bid placed successfully!", Toast.LENGTH_SHORT).show()
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

    private fun updateUIWithBidDetails(bidDetails: Any) {
        // TODO: Update UI with actual bid details from API
        // This is a placeholder - update with actual implementation based on your data model
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

