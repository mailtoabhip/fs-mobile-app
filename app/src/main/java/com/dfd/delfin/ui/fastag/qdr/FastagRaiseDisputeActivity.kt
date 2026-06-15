package com.dfd.delfin.ui.fastag.qdr

import android.Manifest
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dfd.delfin.R
import com.dfd.delfin.api.response.DisputeType
import com.dfd.delfin.api.response.TransactionDisputeResponse
import com.dfd.delfin.databinding.ActivityFastagTransactionDetailBinding
import com.dfd.delfin.databinding.ItemDisputeIssueBinding
import com.dfd.delfin.databinding.ItemDisputeTimelineBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.fastag.fastag_details.FastagTransactionDetailsViewModel
import com.dfd.delfin.utils.DateUtils
import com.dfd.delfin.utils.EVENT_FASTAG_TXN_DETAILS_SHOWN
import com.dfd.delfin.utils.PROPERTY_FASTAG_ID
import com.dfd.delfin.utils.PROPERTY_PAGE_NAME
import com.dfd.delfin.utils.PROPERTY_TRANSACTION_ID
import com.dfd.delfin.utils.PROPERTY_TRANSACTION_TYPE
import com.dfd.delfin.utils.PROPERTY_USER_ID
import com.dfd.delfin.utils.VALUE_FASTAG_TXN_DETAILS_PAGE
import com.dfd.delfin.utils.WindowInsetsUtils
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject

class DisputeIssuesAdapter(
    private val onItemClick: (DisputeType) -> Unit
) : ListAdapter<DisputeType, DisputeIssuesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDisputeIssueBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemDisputeIssueBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(disputeType: DisputeType) {
            binding.tvDisputeName.text = disputeType.displayName

            binding.root.setOnClickListener {
                onItemClick(disputeType)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DisputeType>() {
        override fun areItemsTheSame(oldItem: DisputeType, newItem: DisputeType): Boolean {
            return oldItem.code == newItem.code
        }

        override fun areContentsTheSame(oldItem: DisputeType, newItem: DisputeType): Boolean {
            return oldItem == newItem
        }
    }
}

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

    @Inject
    lateinit var userPrefs: UserPrefs

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

        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

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
            val chipDrawable = GradientDrawable()
            chipDrawable.shape = GradientDrawable.RECTANGLE
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
                    val popup = PopupMenu(this, anchor, Gravity.END)
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
                    val itemBinding = ItemDisputeTimelineBinding.inflate(layoutInflater, binding.layoutTimeline, false)

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
                if (resultCode == RESULT_OK) {
                    viewModel.getTransactionDispute(txnId)
                }
            }
        }
    }

    private fun formatDateTime(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        return DateUtils.formatFastagTransactionDate(dateString)
    }

    private fun showEscalateDialog(ticketId: String) {
        dialogUtils.showEscalateDialog(
            "Want to escalate the dispute?",
            "Please reach out to our support team. Please keep your ticket ID #$ticketId ready"
        ) { d ->
            compositeDisposable plusAssign requestPermission(arrayOf(Manifest.permission.CALL_PHONE))
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