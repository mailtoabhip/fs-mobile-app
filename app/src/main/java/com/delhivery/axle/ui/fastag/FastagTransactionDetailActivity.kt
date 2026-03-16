package com.delhivery.axle.ui.fastag

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.delhivery.axle.R
import com.delhivery.axle.api.response.TransactionDisputeResponse
import com.delhivery.axle.databinding.ActivityFastagTransactionDetailBinding
import com.delhivery.axle.ui.base.BaseActivity
import java.text.SimpleDateFormat
import java.util.Locale

class FastagTransactionDetailActivity : BaseActivity<ActivityFastagTransactionDetailBinding, FastagTransactionDetailsViewModel>() {

    override fun getViewModelClass() = FastagTransactionDetailsViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_transaction_detail
    override fun requireConnection() = false

    companion object {
        const val EXTRA_TXN_ID = "txn_id"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_TOLL_NAME = "toll_name"
        const val EXTRA_TIMESTAMP = "timestamp"
        const val EXTRA_VEHICLE_NUMBER = "vehicle_number"
        const val EXTRA_TRUCK_SIZE = "truck_size"
        const val EXTRA_CAPACITY = "capacity"
        const val EXTRA_OWNERSHIP = "ownership"
    }

    private var txnId: String = ""
    private var currentResponse: TransactionDisputeResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        txnId = intent.getStringExtra(EXTRA_TXN_ID) ?: ""

        setupStaticUI()
        observeData()
        viewModel.getTransactionDispute(txnId)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        if (com.delhivery.axle.utils.WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            com.delhivery.axle.utils.WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
        }
    }

    private fun setupStaticUI() {
        // Populate fields from intent extras (passed from the transaction list)
        val vehicleNumber = intent.getStringExtra(EXTRA_VEHICLE_NUMBER) ?: ""
        val truckSize = intent.getStringExtra(EXTRA_TRUCK_SIZE) ?: ""
        val capacity = intent.getDoubleExtra(EXTRA_CAPACITY, 0.0)
        val ownership = intent.getStringExtra(EXTRA_OWNERSHIP) ?: ""

        binding.tvVehicleNumber.text = vehicleNumber
        binding.tvVehicleMeta.text = "$ownership | $truckSize | $capacity MT"

        binding.ivBack.setOnClickListener { finish() }

        binding.btnRaiseDispute.setOnClickListener {
            val intent = Intent(this, FastagDisputeIssuesActivity::class.java).apply {
                putExtra(FastagDisputeIssuesActivity.EXTRA_PARTNER, currentResponse?.fastagIssuedBy ?: "IDFC")
                putExtra(FastagDisputeIssuesActivity.EXTRA_TXN_ID, currentResponse?.txnId)
                putExtra(FastagDisputeIssuesActivity.EXTRA_FASTAG_ID, currentResponse?.fastagId ?: "")
                putExtra(FastagDisputeIssuesActivity.TOLL_PLAZA_ID, currentResponse?.tollPlazaId ?: "")
            }
            startActivity(intent)
        }
    }

    private fun observeData() {
        viewModel.progressData.observe(this) { isLoading ->
            binding.scrollContent.visibility = if (isLoading) View.GONE else View.VISIBLE
            if (isLoading) {
                binding.btnRaiseDispute.visibility = View.GONE
            }
        }

        viewModel.transactionDisputeData.observe(this) { response ->
            populateUI(response)
        }
    }

    private fun populateUI(response: TransactionDisputeResponse) {
        currentResponse = response

        // FASTag info
        binding.tvFastagProvider.text = "${response.fastagIssuedBy ?: ""} FASTag by Delhivery"
        binding.tvFastagId.text = "FASTag ID: ${response.fastagId ?: ""}"

        // Amount
        val amount = response.txnAmount ?: 0.0
        val isDebit = response.txnType == "Debit"
        binding.tvAmount.text = if (isDebit) "-₹${amount.toInt()}" else "+₹${amount.toInt()}"

        // Transaction details
        binding.tvTxnCategory.text = response.txnCategory ?: ""
        binding.tvTollName.text = response.tollPlazaName ?: ""
        binding.tvTransactionId.text = response.txnId ?: ""
        binding.tvDateTime.text = formatDateTime(response.txnDatetime)

        // Show raise dispute button only for TOLL DEBIT transactions
        binding.btnRaiseDispute.visibility = if (response.txnCategory.equals("TOLL DEBIT", ignoreCase = true)) View.VISIBLE else View.GONE

        // Dispute tracker
        val dispute = response.disputeDetails
        if (dispute != null) {
            binding.cardDisputeTracker.visibility = View.VISIBLE
            binding.tvIssueCategory.text = dispute.issueCategory ?: ""
            binding.tvTicketId.text = "#${dispute.srId ?: ""}"
            binding.tvComment.text = dispute.comment ?: ""

            // Populate timeline from API
            val timeline = dispute.statusTimeline
            if (!timeline.isNullOrEmpty()) {
                timeline.getOrNull(0)?.let {
                    binding.tvSubmittedTime.text = "${it.displayLabel ?: "Submitted"}: ${formatDateTime(it.timestamp)}"
                }
                timeline.getOrNull(1)?.let {
                    binding.tvAcceptedTime.text = "${it.displayLabel ?: "Accepted"}: ${formatDateTime(it.timestamp)}"
                }
                timeline.getOrNull(2)?.let {
                    binding.tvUnderReviewTime.text = "${it.displayLabel ?: "Under Review"}: ${formatDateTime(it.timestamp)}"
                }
            }
        } else {
            binding.cardDisputeTracker.visibility = View.GONE
        }
    }

    private fun formatDateTime(isoString: String?): String {
        if (isoString.isNullOrEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("d MMM yyyy, hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(isoString)
            if (date != null) outputFormat.format(date) else isoString
        } catch (e: Exception) {
            isoString
        }
    }
}
