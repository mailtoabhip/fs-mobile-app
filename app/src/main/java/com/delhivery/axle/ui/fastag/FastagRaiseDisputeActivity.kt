package com.delhivery.axle.ui.fastag

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.delhivery.axle.R
import com.delhivery.axle.api.response.TransactionDisputeResponse
import com.delhivery.axle.databinding.ActivityFastagTransactionDetailBinding
import com.delhivery.axle.ui.base.BaseActivity
import androidx.core.graphics.toColorInt
import com.delhivery.axle.utils.EVENT_FASTAG_TXN_DETAILS_SHOWN
import com.delhivery.axle.utils.EVENT_FASTAG_TXN_LIST_SHOWN
import com.delhivery.axle.utils.PROPERTY_FASTAG_ID
import com.delhivery.axle.utils.PROPERTY_PAGE_NAME
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_ID
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_TYPE
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.VALUE_FASTAG_TXN_DETAILS_PAGE
import com.delhivery.axle.utils.VALUE_FASTAG_TXN_LIST_PAGE
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class FastagRaiseDisputeActivity : BaseActivity<ActivityFastagTransactionDetailBinding, FastagTransactionDetailsViewModel>() {

    override fun getViewModelClass() = FastagTransactionDetailsViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_transaction_detail
    override fun requireConnection() = false

    companion object Companion {
        const val EXTRA_TXN_ID = "txn_id"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_TOLL_NAME = "toll_name"
        const val EXTRA_TIMESTAMP = "timestamp"
        const val EXTRA_VEHICLE_NUMBER = "vehicle_number"
        const val EXTRA_TRUCK_TYPE = "truck_type"
        const val EXTRA_TRUCK_SIZE = "truck_size"
        const val EXTRA_CAPACITY = "capacity"
        const val EXTRA_OWNERSHIP = "ownership"
        const val EXTRA_TRANSACTION_TYPE = "txn_type"
        private const val REQCODE_DISPUTE = 1001
    }

    @Inject lateinit var userPrefs: UserPrefs

    private var txnId: String = ""

    private var txnType: String = ""

    private var currentResponse: TransactionDisputeResponse? = null

    private var fastagId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        txnId = intent.getStringExtra(EXTRA_TXN_ID) ?: ""
        txnType = intent.getStringExtra(EXTRA_TRANSACTION_TYPE) ?: ""

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
        val truckType = intent.getStringExtra(EXTRA_TRUCK_TYPE) ?: ""
        val truckSize = intent.getStringExtra(EXTRA_TRUCK_SIZE) ?: ""
        val capacity = intent.getDoubleExtra(EXTRA_CAPACITY, 0.0)
        val ownership = intent.getStringExtra(EXTRA_OWNERSHIP) ?: ""

        binding.tvVehicleNumber.text = vehicleNumber
        binding.tvVehicleMeta.text = "$ownership | $truckSize | $capacity MT"

        // Set truck image based on vehicle type
        val truckImageRes = when (truckType) {
            "closed" -> R.drawable.container
            "open" -> R.drawable.open_body
            else -> R.drawable.trailer
        }
        binding.ivTruckImage.setImageResource(truckImageRes)

        binding.ivBack.setOnClickListener { finish() }

        binding.btnRaiseDispute.setOnClickListener {
            val intent = Intent(this, FastagDisputeIssuesActivity::class.java).apply {
                putExtra(FastagDisputeIssuesActivity.EXTRA_PARTNER, currentResponse?.fastagIssuedBy ?: "IDFC")
                putExtra(FastagDisputeIssuesActivity.EXTRA_TXN_ID, currentResponse?.txnId)
                putExtra(FastagDisputeIssuesActivity.EXTRA_FASTAG_ID, currentResponse?.fastagId ?: "")
                putExtra(FastagDisputeIssuesActivity.TOLL_PLAZA_ID, currentResponse?.tollPlazaId ?: "")
            }
            startActivityForResult(intent, REQCODE_DISPUTE)
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

            analyticsUtil.moEngageTrackEvent(
                EVENT_FASTAG_TXN_DETAILS_SHOWN,
                mutableListOf(
                    PROPERTY_USER_ID,
                    PROPERTY_PAGE_NAME,
                    PROPERTY_TRANSACTION_TYPE,
                    PROPERTY_TRANSACTION_ID,
                    PROPERTY_FASTAG_ID
                ),
                mutableListOf(
                    userPrefs.userId(),
                    VALUE_FASTAG_TXN_DETAILS_PAGE,
                    txnType,
                    txnId,
                    fastagId
                )
            )
        }
    }

    private fun populateUI(response: TransactionDisputeResponse) {
        currentResponse = response

        // FASTag info
        binding.tvFastagProvider.text = "${response.fastagIssuedBy ?: ""} FASTag by Delhivery"
        binding.tvFastagId.text = "FASTag ID: ${response.fastagId ?: ""}"
        fastagId = response.fastagId ?: ""

        // Amount
        val amount = response.txnAmount ?: 0.0
        val isDebit = response.txnType == "Debit"
        binding.tvAmount.text = if (isDebit) "-₹${amount.toInt()}" else "+₹${amount.toInt()}"

        // Transaction details
        binding.tvTxnCategory.text = response.txnCategory ?: ""
        if (!response.tollPlazaName.isNullOrEmpty()) {
            binding.layoutTollName.visibility = View.VISIBLE
            binding.tvTollName.text = response.tollPlazaName
        } else {
            binding.layoutTollName.visibility = View.GONE
        }
        binding.tvTransactionId.text = response.txnId ?: ""
        binding.tvDateTime.text = formatDateTime(response.txnDatetime)

        binding.btnRaiseDispute.visibility = if (response.txnCategory.equals("TOLL DEBIT", ignoreCase = true) && response.disputeDetails == null) View.VISIBLE else View.GONE

        // Dispute status chip below amount
        val dispute = response.disputeDetails
        if (dispute != null && !dispute.currentStatus.isNullOrEmpty()) {
            binding.tvDisputeStatusChip.visibility = View.VISIBLE
            binding.tvDisputeStatusChip.text = dispute.currentStatus

            val chipBgColor: Int
            val chipTextColor: Int
            when (dispute.currentStatusColor?.lowercase()) {
                "pending" -> {
                    chipBgColor = getColor(R.color.pending_bg)
                    chipTextColor = "#B45309".toColorInt()
                }
                "success" -> {
                    chipBgColor = "#DCFCE7".toColorInt()
                    chipTextColor = "#16A34A".toColorInt()
                }
                "failed" -> {
                    chipBgColor = "#FEE2E2".toColorInt()
                    chipTextColor = "#DC2626".toColorInt()
                }
                else -> {
                    chipBgColor = "#EFEFEF".toColorInt()
                    chipTextColor = getColor(R.color.black_text)
                }
            }
            val chipDrawable = android.graphics.drawable.GradientDrawable()
            chipDrawable.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            chipDrawable.cornerRadius = resources.getDimension(R.dimen.size_16dp)
            chipDrawable.setColor(chipBgColor)
            binding.tvDisputeStatusChip.background = chipDrawable
            binding.tvDisputeStatusChip.setTextColor(chipTextColor)
        } else {
            binding.tvDisputeStatusChip.visibility = View.GONE
        }

        // Dispute tracker
        if (dispute != null) {
            binding.cardDisputeTracker.visibility = View.VISIBLE
            binding.tvIssueCategory.text = dispute.issueCategory ?: ""
            binding.tvTicketId.text = "#${dispute.srId ?: ""}"
            binding.tvComment.text = dispute.comment ?: ""

            // Show escalate button if last status is Rejected
            val timeline = dispute.statusTimeline
            val lastStatus = timeline?.lastOrNull()?.status
            if (lastStatus.equals("Rejected", ignoreCase = true)) {
                binding.ivDisputeKebab.visibility = View.VISIBLE
                binding.ivDisputeKebab.setOnClickListener { anchor ->
                    val popup = android.widget.PopupMenu(this, anchor, android.view.Gravity.END)
                    popup.menuInflater.inflate(R.menu.menu_dispute_escalate, popup.menu)
                    popup.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.action_escalate_ticket -> {
                                showEscalateDialog(dispute.srId ?: "")
                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()
                }
            } else {
                binding.ivDisputeKebab.visibility = View.GONE
            }

            // Populate dynamic timeline from API
            if (!timeline.isNullOrEmpty()) {
                binding.layoutTimeline.removeAllViews()
                binding.layoutTimeline.visibility = View.VISIBLE
                for (i in timeline.indices) {
                    val item = timeline[i]
                    val isLast = i == timeline.size - 1
                    val itemBinding = com.delhivery.axle.databinding.ItemDisputeTimelineBinding.inflate(layoutInflater, binding.layoutTimeline, false)

                    itemBinding.tvStatusTitle.text = item.status ?: ""

                    val formattedTime = formatDateTime(item.changedAt)
                    if (formattedTime.isNotEmpty()) {
                        itemBinding.tvStatusTime.text = "${item.status ?: ""}: $formattedTime"
                        itemBinding.tvStatusTime.visibility = View.VISIBLE
                    } else {
                        itemBinding.tvStatusTime.visibility = View.GONE
                    }

                    if (!item.message.isNullOrEmpty()) {
                        itemBinding.tvStatusComment.text = item.message
                        itemBinding.tvStatusComment.visibility = View.VISIBLE
                    } else {
                        itemBinding.tvStatusComment.visibility = View.GONE
                    }

                    // Icon: green check for all items, status-based icon only for last item
                    if (isLast) {
                        when (item.status?.lowercase()) {
                            "settled - full refund" -> {
                                itemBinding.ivStatusIcon.setImageResource(R.drawable.ic_check_circle_green)
                                itemBinding.ivStatusIcon.imageTintList = android.content.res.ColorStateList.valueOf("#10B981".toColorInt())
                            }
                            "rejected" -> {
                                itemBinding.ivStatusIcon.setImageResource(R.drawable.dispute_rejected)
                                itemBinding.ivStatusIcon.imageTintList = null
                            }
                            "additional evidence required" -> {
                                itemBinding.ivStatusIcon.setImageResource(R.drawable.additional_info)
                                itemBinding.ivStatusIcon.imageTintList = null
                            }
                            else -> {
                                itemBinding.ivStatusIcon.setImageResource(R.drawable.ic_dispute_under_review)
                                itemBinding.ivStatusIcon.imageTintList = null
                            }
                        }
                    } else {
                        itemBinding.ivStatusIcon.setImageResource(R.drawable.ic_check_circle_green)
                        itemBinding.ivStatusIcon.imageTintList = android.content.res.ColorStateList.valueOf("#10B981".toColorInt())
                    }

                    // Hide connector line for last item
                    itemBinding.viewConnector.visibility = if (isLast) View.GONE else View.VISIBLE

                    binding.layoutTimeline.addView(itemBinding.root)
                }
            } else {
                binding.layoutTimeline.visibility = View.GONE
            }
        } else {
            binding.cardDisputeTracker.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQCODE_DISPUTE -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.getTransactionDispute(txnId)
                }
            }
        }
    }

    private fun formatDateTime(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        return com.delhivery.axle.utils.DateUtils.formatFastagTransactionDate(dateString)
    }

    private fun showEscalateDialog(ticketId: String) {
        dialogUtils.showEscalateDialog(
            "Want to escalate the dispute?",
            "Please reach out to our support team. Please keep your ticket ID #$ticketId ready"
        ) { d ->
            compositeDisposable += requestPermission(arrayOf(android.Manifest.permission.CALL_PHONE))
                .onBackground()
                .subscribe { granted, error ->
                    d.dismiss()
                    if (error == null && granted) {
                        if (!contactUtils.callHelpline()) {
                            uiUtils.showSnackbar("Unable to place call")
                        }
                    } else {
                        uiUtils.showSnackbar(getString(R.string.msg_call_permission))
                    }
                }
        }
    }
}
