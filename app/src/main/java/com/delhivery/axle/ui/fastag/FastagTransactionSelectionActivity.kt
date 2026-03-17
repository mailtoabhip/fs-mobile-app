package com.delhivery.axle.ui.fastag

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityFastagTransactionSelectionBinding
import com.delhivery.axle.ui.base.BaseActivity

class FastagTransactionSelectionActivity : BaseActivity<ActivityFastagTransactionSelectionBinding, FastagTransactionSelectionViewModel>() {

    override fun getViewModelClass() = FastagTransactionSelectionViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_transaction_selection
    override fun requireConnection() = true

    private lateinit var adapter: TransactionSelectionAdapter
    private var title: String = ""
    private var subTitle: String = ""
    private var fastagId: String = ""
    private var tollPlazaId: String = ""
    private var disputeTypeCode: String = ""
    private var txnId: String = ""

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_SUBTITLE = "subtitle"
        const val EXTRA_PARTNER = "partner"
        const val EXTRA_DISPUTE_CODE = "dispute_code"
        const val EXTRA_FASTAG_ID = "fastag_id"
        const val EXTRA_TOLL_PLAZA_ID = "toll_plaza_id"
        const val EXTRA_TXN_ID = "txn_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
        observeData()
        loadTransactions()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        /* Handle window insets for edge-to-edge display (API 35+) */
        if (com.delhivery.axle.utils.WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            com.delhivery.axle.utils.WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
        }
    }

    private fun setupUI() {
        title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        subTitle = intent.getStringExtra(EXTRA_SUBTITLE) ?: ""
        fastagId = intent.getStringExtra(EXTRA_FASTAG_ID) ?: ""
        tollPlazaId = intent.getStringExtra(EXTRA_TOLL_PLAZA_ID) ?: ""
        disputeTypeCode = intent.getStringExtra(EXTRA_DISPUTE_CODE) ?: ""
        txnId = intent.getStringExtra(EXTRA_TXN_ID) ?: ""

        binding.tvTitle.text = "Fastag related issues"

        binding.ivBack.setOnClickListener {
            finish()
        }

        adapter = TransactionSelectionAdapter { transaction ->
            onTransactionSelected(transaction)
        }

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(this@FastagTransactionSelectionActivity)
            adapter = this@FastagTransactionSelectionActivity.adapter
        }

        // Initially disable the confirm button
        binding.btnConfirmSelection.isEnabled = false

        binding.btnConfirmSelection.setOnClickListener {
            val selectedTransaction = adapter.getSelectedTransaction()
            if (selectedTransaction != null) {
                // Navigate to dynamic dispute form
                val intent = Intent(this, FastagDynamicDisputeFormActivity::class.java).apply {
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_DISPUTE_TYPE_CODE, disputeTypeCode)
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_SELECTED_TRANSACTION_ID, selectedTransaction.id)
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_FASTAG_ID, fastagId)
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_TOLL_PLAZA_ID, selectedTransaction.tollPlazaId ?: "")
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_TRANSACTION_TOLL_NAME, selectedTransaction.tollName)
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_TRANSACTION_TIMESTAMP, selectedTransaction.timestamp)
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_TRANSACTION_AMOUNT, selectedTransaction.amount ?: 0.0)
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_TXN_ID, txnId)
                }
                startActivity(intent)
            }
        }
    }

    private fun loadTransactions() {
        viewModel.getTransactionsByTollPlaza(tollPlazaId = tollPlazaId, fastagId = fastagId)
    }

    private fun observeData() {
        viewModel.progressData.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvTransactions.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.transactionsByTollPlazaData.observe(this) { response ->
            response.transactions?.let { transactions ->
                val transactionItems = transactions.map { txn ->
                    TransactionItem(
                        id = txn.txnId ?: "",
                        tollName = txn.tollPlazaName ?: "Unknown",
                        timestamp = formatDateTime(txn.txnDateTime ?: ""),
                        amount = if (txn.txnType == "DEBIT") -(txn.txnAmount ?: 0.0) else (txn.txnAmount ?: 0.0),
                        tollPlazaId = txn.tollPlazaId
                    )
                }
                adapter.submitList(transactionItems)
            }
        }
    }

    /**
     * Format ISO 8601 datetime to readable format
     * Example: "2025-01-20T10:30:45Z" -> "20 Jan 2025, 10:30 AM"
     */
    private fun formatDateTime(dateTime: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
            inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateTime)
            
            val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: dateTime
        } catch (e: Exception) {
            dateTime
        }
    }

    private fun onTransactionSelected(transaction: TransactionItem) {
        adapter.setSelectedTransaction(transaction)
        // Enable confirm button when a transaction is selected
        binding.btnConfirmSelection.isEnabled = true
    }
}
